package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;

public class InimigoVoador extends Inimigo {
    // Estados do inimigo
    private static final int PATRULHA = 0;
    private static final int PERSEGUIR = 1;
    private static final int ATACAR   = 2;
    private static final int RECUAR   = 3;

    // Raios de detecção (centrados no inimigo)
    private final float RAIO_ATAQUE;    // Círculo interno (para ataque)
    private final float RAIO_PERSEGUIR; // Círculo externo (para perseguição)

    // Configurações de ataque e cooldown
    private static final float VELOCIDADE_ATAQUE = 300f;
    private static final float CD_ATAQUE = 3f;
    // Valor fallback para altitude caso nenhuma plataforma seja encontrada
    private static final float ALTURA_FALLBACK = 150f;
    // Margem adicional acima do “chão” para o voo
    private static final float ALTURA_MARGEM = 20f;

    // Parâmetros para evasão de obstáculos
    private static final float AVOIDANCE_RADIUS = 50f;      // Raio de influência para evasão
    private static final float AVOIDANCE_STRENGTH = 100f;     // Força máxima da repulsão

    private int estado;
    private float tempoCD;
    private Vector2 alvoAtaque;    // Ponto para o ataque (um pouco dentro do círculo de ataque)
    private Vector2 alvoRetorno;   // Ponto para recuar (na borda do círculo de ataque)
    private Vector2 direcaoAtaque; // Direção do ataque
    private float velocidadePatrulha;
    // Posição base para patrulha (definida quando perde o player)
    private Vector2 posicaoInicial;
    // Timer para a oscilação vertical na patrulha
    private float tempoPatrulhaTimer = 0f;

    public InimigoVoador(Texture texture, float x, float y) {
        super(texture, x, y, 1.5f, 150f, 3);
        // Define os raios com base no tamanho do sprite
        RAIO_ATAQUE = sprite.getWidth() * scale * 4f;
        RAIO_PERSEGUIR = RAIO_ATAQUE * 3f;

        // Inicializa os vetores de ataque
        alvoAtaque = new Vector2();
        alvoRetorno = new Vector2();
        direcaoAtaque = new Vector2();

        inicializar(x, y);
    }

    private void inicializar(float x, float y) {
        estado = PATRULHA;
        // Usa ALTURA_FALLBACK se y for muito baixo
        posicaoInicial = new Vector2(x, Math.max(y, ALTURA_FALLBACK));
        velocidadePatrulha = speed * 0.5f;
        sprite.setPosition(posicaoInicial.x, posicaoInicial.y);
        atualizaHitbox();
    }

    @Override
    public void update(float delta, Player player, Rectangle[] colisoes, Rectangle limitesCamera) {
        if (!isAlive) return;

        Vector2 centroInimigo = getCentro();
        Vector2 centroJogador = player.getCenterPosition();
        float distancia = centroInimigo.dst(centroJogador);

        // Atualiza o estado, agora também considerando linha de visão
        atualizarEstados(delta, distancia, player, colisoes);
        // Executa comportamento com evasão integrada
        executarComportamento(delta, player, centroInimigo, centroJogador, distancia, colisoes);

        // Resolve colisões e ajusta altitude de voo
        aplicarColisoes(colisoes);
        manterAlturaDesejada(colisoes, player, delta);
        atualizaHitbox();
    }

    // Atualiza o estado com base na distância e se há linha de visão livre
    private void atualizarEstados(float delta, float distancia, Player player, Rectangle[] obstacles) {
        tempoCD += delta;
        switch (estado) {
            case PATRULHA:
                if (distancia <= RAIO_PERSEGUIR) {
                    estado = PERSEGUIR;
                }
                break;
            case PERSEGUIR:
                if (distancia > RAIO_PERSEGUIR) {
                    estado = PATRULHA;
                    // Define a posição base para patrulha como a posição atual (sem saltos bruscos)
                    posicaoInicial.set(sprite.getX(), sprite.getY());
                    tempoPatrulhaTimer = 0f;
                }
                // Só inicia o ataque se estiver dentro do raio e com linha de visão livre
                else if (distancia <= RAIO_ATAQUE && tempoCD >= CD_ATAQUE &&
                        hasLineOfSight(getCentro(), player.getCenterPosition(), obstacles)) {
                    prepararAtaque(player.getCenterPosition());
                }
                break;
            // Estados ATACAR e RECUAR fazem a transição internamente
        }
    }

    // Prepara os pontos de ataque com base na posição do player
    private void prepararAtaque(Vector2 alvo) {
        estado = ATACAR;
        tempoCD = 0;
        Vector2 direcao = new Vector2(alvo).sub(getCentro()).nor();
        float offsetAtaque = 20f; // Desloca um pouco para dentro do círculo de ataque
        alvoAtaque.set(alvo).sub(new Vector2(direcao).scl(offsetAtaque));
        alvoRetorno.set(alvo).sub(new Vector2(direcao).scl(RAIO_ATAQUE));
        direcaoAtaque.set(direcao);
    }

    // Executa o comportamento atual (patrulha, perseguição, ataque ou recuo) com evasão
    private void executarComportamento(float delta, Player player, Vector2 centroInimigo, Vector2 centroJogador, float distancia, Rectangle[] obstacles) {
        switch (estado) {
            case PATRULHA:
                patrulhar(delta, obstacles);
                break;
            case PERSEGUIR:
                perseguir(delta, centroJogador, centroInimigo, obstacles);
                break;
            case ATACAR:
                atacar(delta);
                break;
            case RECUAR:
                recuar(delta);
                break;
        }
    }

    // MOVIMENTO DE PATRULHA
    // O inimigo se move horizontalmente em torno de uma posição base fixa e oscila verticalmente suavemente.
    // O vetor de movimento é ajustado para desviar de obstáculos.
    private void patrulhar(float delta, Rectangle[] obstacles) {
        tempoPatrulhaTimer += delta;
        float amplitude = 20f;     // Amplitude da oscilação vertical
        float frequencia = 2f;      // Frequência da oscilação
        float targetY = posicaoInicial.y + amplitude * MathUtils.sin(tempoPatrulhaTimer * frequencia);
        float moveY = targetY - sprite.getY();

        // Movimento horizontal limitado (±100 unidades a partir da posicaoInicial.x)
        float range = 100f;
        float moveX = velocidadePatrulha * delta;
        float novoX = sprite.getX() + moveX;
        if (novoX > posicaoInicial.x + range) {
            novoX = posicaoInicial.x + range;
            velocidadePatrulha = -Math.abs(velocidadePatrulha);
        } else if (novoX < posicaoInicial.x - range) {
            novoX = posicaoInicial.x - range;
            velocidadePatrulha = Math.abs(velocidadePatrulha);
        }
        Vector2 desiredMovement = new Vector2(novoX - sprite.getX(), moveY);
        Vector2 adjustedMovement = applyObstacleAvoidance(desiredMovement, obstacles);
        sprite.translate(adjustedMovement.x, adjustedMovement.y);
    }

    // MOVIMENTO DE PERSEGUIÇÃO
    // O inimigo se desloca em direção ao player, mas o movimento é modificado pela evasão de obstáculos.
    private void perseguir(float delta, Vector2 alvo, Vector2 centro, Rectangle[] obstacles) {
        Vector2 direcao = new Vector2(alvo).sub(centro).nor();
        Vector2 desiredMovement = new Vector2(direcao).scl(speed * delta);
        Vector2 adjustedMovement = applyObstacleAvoidance(desiredMovement, obstacles);
        sprite.translate(adjustedMovement.x, adjustedMovement.y);
    }

    // ESTADO DE ATAQUE
    // Avança até o ponto de ataque e, ao chegar, muda para recuar.
    private void atacar(float delta) {
        Vector2 pos = getCentro();
        Vector2 toTarget = new Vector2(alvoAtaque).sub(pos);
        if (toTarget.len() > 5f) {
            toTarget.nor();
            sprite.translate(toTarget.x * VELOCIDADE_ATAQUE * delta,
                    toTarget.y * VELOCIDADE_ATAQUE * delta);
        } else {
            estado = RECUAR;
        }
    }

    // ESTADO DE RECUO
    // Retorna à borda do círculo de ataque; ao chegar, volta a perseguir.
    private void recuar(float delta) {
        Vector2 pos = getCentro();
        Vector2 toTarget = new Vector2(alvoRetorno).sub(pos);
        if (toTarget.len() > 5f) {
            toTarget.nor();
            sprite.translate(toTarget.x * speed * delta * 0.7f,
                    toTarget.y * speed * delta * 0.7f);
        } else {
            estado = PERSEGUIR;
        }
    }

    // MÉTODO DE EVASÃO DE OBSTÁCULOS
    // Para cada obstáculo, se o inimigo estiver muito próximo (dentro de AVOIDANCE_RADIUS),
    // calcula um vetor de repulsão proporcional à proximidade. Se o obstáculo estiver "atrás" ou bloqueando a linha,
    // o vetor tende a empurrar o inimigo para uma direção que contorne o obstáculo – inclusive com componente vertical.
    private Vector2 applyObstacleAvoidance(Vector2 desiredMovement, Rectangle[] obstacles) {
        Vector2 enemyCenter = getCentro();
        Vector2 avoidanceForce = new Vector2();
        for (Rectangle obstaculo : obstacles) {
            // Calcula o ponto mais próximo do enemyCenter no retângulo do obstáculo
            float closestX = MathUtils.clamp(enemyCenter.x, obstaculo.x, obstaculo.x + obstaculo.width);
            float closestY = MathUtils.clamp(enemyCenter.y, obstaculo.y, obstaculo.y + obstaculo.height);
            Vector2 closestPoint = new Vector2(closestX, closestY);
            float distance = enemyCenter.dst(closestPoint);
            if (distance < AVOIDANCE_RADIUS) {
                if (distance < 0.1f) distance = 0.1f;
                // Calcula a direção de repulsão (do ponto mais próximo para longe)
                Vector2 pushDir = enemyCenter.cpy().sub(closestPoint).nor();
                // Se o obstáculo estiver acima e bloqueando o avanço, incentive a subida
                if (closestPoint.y > enemyCenter.y) {
                    pushDir.y = Math.abs(pushDir.y);
                }
                float repulsionMagnitude = AVOIDANCE_STRENGTH * (AVOIDANCE_RADIUS - distance) / AVOIDANCE_RADIUS;
                avoidanceForce.add(pushDir.scl(repulsionMagnitude));
            }
        }
        // Combina o movimento desejado com a força de evasão
        Vector2 result = new Vector2(desiredMovement).add(avoidanceForce);
        return result;
    }

    // MÉTODO DE RESOLUÇÃO DE COLISÕES
    // Se o hitbox do inimigo sobrepõe um obstáculo, calcula um vetor de correção para empurrá-lo suavemente para fora.
    private void aplicarColisoes(Rectangle[] colisoes) {
        Rectangle hitbox = getHitboxRect();
        for (Rectangle obstaculo : colisoes) {
            // Se o centro estiver significativamente acima do obstáculo, ignora para não interferir no voo
            if (getCentro().y > obstaculo.y + obstaculo.height + 10) continue;
            if (hitbox.overlaps(obstaculo)) {
                resolverColisao(hitbox, obstaculo);
            }
        }
    }

    // Resolucão simples: calcula a sobreposição nos eixos e aplica uma correção proporcional
    private void resolverColisao(Rectangle inimigo, Rectangle obstaculo) {
        Vector2 correcao = new Vector2();
        float sobreposX = calcularSobreposicao(inimigo.x, inimigo.width, obstaculo.x, obstaculo.width);
        float sobreposY = calcularSobreposicao(inimigo.y, inimigo.height, obstaculo.y, obstaculo.height);
        if (Math.abs(sobreposX) < Math.abs(sobreposY)) {
            correcao.x = sobreposX;
        } else {
            correcao.y = sobreposY;
        }
        // Aplica uma correção suave para evitar saltos bruscos
        sprite.translate(correcao.x * 0.8f, correcao.y * 0.8f);
    }

    // Calcula a sobreposição em um eixo entre o inimigo e um obstáculo
    private float calcularSobreposicao(float aPos, float aTam, float bPos, float bTam) {
        return (aPos < bPos)
                ? bPos - (aPos + aTam)
                : (bPos + bTam) - aPos;
    }

    // RECALCULA A ALTITUDE DE VOO DESEJADA
    // Essa função determina qual deve ser a altitude alvo, levando em conta:
    // • A plataforma imediatamente abaixo (acima do topo da plataforma + ALTURA_MARGEM)
    // • A posição do player (se o player estiver abaixo, o inimigo deve descer suavemente)
    // O alvo será o máximo entre esses dois, garantindo que o inimigo não voe muito baixo nem fique preso.
    private float recalcDesiredAltitude(Rectangle[] obstacles, Player player) {
        float altFromPlatform = ALTURA_FALLBACK;
        Vector2 centro = getCentro();
        // Considera obstáculos que sejam plataformas (assumindo índices a partir de 3)
        for (int i = 3; i < obstacles.length; i++) {
            Rectangle plat = obstacles[i];
            if (centro.x >= plat.x - 5 && centro.x <= plat.x + plat.width + 5) {
                float platTop = plat.y + plat.height + ALTURA_MARGEM;
                if (platTop > altFromPlatform) {
                    altFromPlatform = platTop;
                }
            }
        }
        // Se o player estiver abaixo, tenta ajustar para acompanhar
        float altFromPlayer = player.getCenterPosition().y + ALTURA_MARGEM;
        return Math.max(altFromPlatform, altFromPlayer);
    }

    // Ajusta gradualmente a altitude do inimigo para se aproximar da altitude desejada
    private void manterAlturaDesejada(Rectangle[] obstacles, Player player, float delta) {
        float desiredAltitude = recalcDesiredAltitude(obstacles, player);
        if (sprite.getY() < desiredAltitude) {
            float newY = MathUtils.lerp(sprite.getY(), desiredAltitude, 0.1f * delta);
            sprite.setY(newY);
        } else if (sprite.getY() > desiredAltitude + 20f) {
            // Se estiver muito acima, desce suavemente
            float newY = MathUtils.lerp(sprite.getY(), desiredAltitude, 0.1f * delta);
            sprite.setY(newY);
        }
    }

    // Retorna o centro do sprite do inimigo
    public Vector2 getCentro() {
        return new Vector2(
                sprite.getX() + (sprite.getWidth() * scale) / 2,
                sprite.getY() + (sprite.getHeight() * scale) / 2
        );
    }

    // Verifica se há linha de visão livre entre dois pontos (sem interseção com obstáculos)
    private boolean hasLineOfSight(Vector2 from, Vector2 to, Rectangle[] obstacles) {
        for (Rectangle obstaculo : obstacles) {
            if (lineIntersectsRectangle(from, to, obstaculo)) {
                return false;
            }
        }
        return true;
    }

    // Verifica se o segmento definido por p1 e p2 intersecta o retângulo
    private boolean lineIntersectsRectangle(Vector2 p1, Vector2 p2, Rectangle rect) {
        Vector2 r1 = new Vector2(rect.x, rect.y);
        Vector2 r2 = new Vector2(rect.x + rect.width, rect.y);
        Vector2 r3 = new Vector2(rect.x + rect.width, rect.y + rect.height);
        Vector2 r4 = new Vector2(rect.x, rect.y + rect.height);
        return linesIntersect(p1, p2, r1, r2) ||
                linesIntersect(p1, p2, r2, r3) ||
                linesIntersect(p1, p2, r3, r4) ||
                linesIntersect(p1, p2, r4, r1);
    }

    // Testa se dois segmentos (p1-p2 e p3-p4) se intersectam
    private boolean linesIntersect(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4) {
        float denominator = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x);
        if (Math.abs(denominator) < 0.0001f) return false; // Paralelos ou coincidentes
        float ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denominator;
        float ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denominator;
        return (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1);
    }

    // Desenha os círculos de detecção para depuração: externo (verde) e interno (vermelho)
    public void desenharAreas(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(getCentro().x, getCentro().y, RAIO_PERSEGUIR);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(getCentro().x, getCentro().y, RAIO_ATAQUE);
    }
}
