package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class InimigoTerrestre extends Inimigo {
    // Estados da máquina de estados
    private static final int STATE_PATROL  = 0;
    private static final int STATE_CHASE   = 1;
    private static final int STATE_SPRINT  = 2;
    private static final int STATE_WAIT    = 3;

    // Raios dos círculos de detecção
    // Círculo para iniciar perseguição
    private final float DETECTION_RADIUS;
    // Círculo para iniciar sprint/ataque (menor)
    private final float SPRINT_RADIUS;

    // Velocidades (pixels/segundo)
    private float patrolSpeed;   // Velocidade de patrulha
    private float chaseSpeed;    // Velocidade de perseguição
    private float sprintSpeed;   // Velocidade de sprint

    // Tempos do sprint e da espera
    private static final float SPRINT_DURATION = 2.5f; // Duração do sprint (em segundos)
    private static final float WAIT_DURATION   = 1.0f; // Tempo de pausa após o sprint
    private float sprintTimer;   // Acumula tempo durante o sprint
    private float waitTimer;     // Acumula tempo durante a espera

    // Estado atual
    private int state;

    // Vetor para a direção do sprint (apenas horizontal)
    private Vector2 sprintDirection;

    public InimigoTerrestre(Texture texture, float x, float y) {
        // Usamos escala 2f para um sprite maior; demais parâmetros podem ser ajustados.
        super(texture, x, y, 2f, 100f, 3);

        // Definindo os raios:
        DETECTION_RADIUS = sprite.getWidth() * scale * 10f; // por exemplo, 6× a largura do sprite
        SPRINT_RADIUS = sprite.getWidth() * scale * 7f;     // 1.2× a largura do sprite

        // Velocidades
        patrolSpeed = 50f;
        chaseSpeed = 100f;
        sprintSpeed = 200f;

        state = STATE_PATROL;
        sprintTimer = 0f;
        waitTimer = 0f;
        sprintDirection = new Vector2();
    }

    @Override
    public void update(float delta, Player player, Rectangle[] obstacles, Rectangle cameraBounds) {
        if (!isAlive) return;

        Vector2 center = getCenter();
        Vector2 playerCenter = player.getCenterPosition();
        float dist = center.dst(playerCenter);
        boolean los = hasLineOfSight(center, playerCenter, obstacles);

        // Máquina de estados com histerese:
        if (state == STATE_SPRINT) {
            // No estado de sprint, se a hitbox de ataque colidir com o player, vai para WAIT imediatamente.
            if (getAttackHitbox().overlaps(player.getPlayerBounds())) {
                state = STATE_WAIT;
                waitTimer = 0f;
            }
        }
        else if (state == STATE_CHASE) {
            // Se o player sair da área de detecção ou perder a linha de visão, volta para patrulha.
            if (dist > DETECTION_RADIUS || !los) {
                state = STATE_PATROL;
            }
            // Se o player estiver bem próximo (dentro do sprint radius), inicia o sprint.
            else if (dist <= SPRINT_RADIUS && los) {
                state = STATE_SPRINT;
                sprintTimer = 0f;
                sprintDirection.set(playerCenter).sub(center);
                sprintDirection.y = 0; // Apenas horizontal
                if (sprintDirection.len() != 0)
                    sprintDirection.nor();
            }
        }
        else if (state == STATE_PATROL) {
            // Se o player estiver dentro do DETECTION_RADIUS e em linha de visão, muda para CHASE.
            if (dist <= DETECTION_RADIUS && los) {
                state = STATE_CHASE;
            }
        }
        else if (state == STATE_WAIT) {
            // Durante o wait, o inimigo não se move. Após WAIT_DURATION, se o player ainda estiver próximo, volta para sprint; caso contrário, vai para chase.
            waitTimer += delta;
            if (waitTimer >= WAIT_DURATION) {
                if (dist <= SPRINT_RADIUS && los) {
                    state = STATE_SPRINT;
                    sprintTimer = 0f;
                    sprintDirection.set(playerCenter).sub(center);
                    sprintDirection.y = 0;
                    if (sprintDirection.len() != 0)
                        sprintDirection.nor();
                } else {
                    state = STATE_CHASE;
                }
            }
        }

        // Comportamento conforme o estado:
        switch(state) {
            case STATE_SPRINT:
                sprintTimer += delta;
                float moveSprint = sprintSpeed * delta;
                Rectangle futureHitboxSprint = new Rectangle(getHitboxRect());
                futureHitboxSprint.x += sprintDirection.x * moveSprint;
                boolean collisionSprint = false;
                for (Rectangle obs : obstacles) {
                    if (isGroundObstacle(obs)) continue;
                    if (futureHitboxSprint.overlaps(obs)) {
                        collisionSprint = true;
                        break;
                    }
                }
                if (!collisionSprint) {
                    sprite.translate(sprintDirection.x * moveSprint, 0);
                }
                if (sprintTimer >= SPRINT_DURATION) {
                    state = STATE_WAIT;
                    waitTimer = 0f;
                }
                break;

            case STATE_CHASE:
                float dx = 0;
                if (playerCenter.x > center.x) {
                    dx = chaseSpeed * delta;
                } else if (playerCenter.x < center.x) {
                    dx = -chaseSpeed * delta;
                }
                Rectangle futureHitboxChase = new Rectangle(getHitboxRect());
                futureHitboxChase.x += dx;
                boolean collisionChase = false;
                for (Rectangle obs : obstacles) {
                    if (isGroundObstacle(obs)) continue;
                    if (futureHitboxChase.overlaps(obs)) {
                        collisionChase = true;
                        break;
                    }
                }
                if (!collisionChase && los) {
                    sprite.translate(dx, 0);
                }
                break;

            case STATE_PATROL:
                float dxPatrol = patrolSpeed * delta;
                Rectangle futureHitboxPatrol = new Rectangle(getHitboxRect());
                futureHitboxPatrol.x += dxPatrol;
                boolean collisionPatrol = false;
                for (Rectangle obs : obstacles) {
                    if (isGroundObstacle(obs)) continue;
                    if (futureHitboxPatrol.overlaps(obs)) {
                        collisionPatrol = true;
                        break;
                    }
                }
                if (collisionPatrol) {
                    // Inverte a direção
                    patrolSpeed = -patrolSpeed;
                    dxPatrol = patrolSpeed * delta;
                }
                sprite.translate(dxPatrol, 0);
                break;

            case STATE_WAIT:
                // Não se move durante o wait.
                // A transição para outro estado é feita acima.
                break;
        }

        atualizaHitbox();
    }

    // Retorna o centro do sprite
    public Vector2 getCenter() {
        return new Vector2(
                sprite.getX() + (sprite.getWidth() * scale) / 2,
                sprite.getY() + (sprite.getHeight() * scale) / 2
        );
    }

    // Retorna a hitbox atual (assumindo que atualizaHitbox atualize hitboxRect)
    public Rectangle getHitboxRect() {
        return hitboxRect;
    }

    // Retorna a hitbox do ataque, que é a hitbox atual estendida 20 pixels na direção do sprint.
    public Rectangle getAttackHitbox() {
        Rectangle base = new Rectangle(getHitboxRect());
        float extension = 20f;
        if (sprintDirection.x >= 0) {
            base.width += extension;
        } else {
            base.x -= extension;
            base.width += extension;
        }
        return base;
    }

    // Desenha os círculos de detecção: externo (verde) e interno (vermelho)
    public void desenharAreas(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(getCenter().x, getCenter().y, DETECTION_RADIUS);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.circle(getCenter().x, getCenter().y, SPRINT_RADIUS);
    }

    // Desenha a hitbox do ataque (retângulo amarelo) para depuração
    public void desenharAttackHitbox(ShapeRenderer shapeRenderer) {
        Rectangle attackBox = getAttackHitbox();
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(attackBox.x, attackBox.y, attackBox.width, attackBox.height);
    }

    // Auxiliar: considera um obstáculo "de chão" se o topo do obstáculo estiver a menos de 10 pixels do fundo do inimigo
    private boolean isGroundObstacle(Rectangle obs) {
        float enemyBottom = sprite.getY();
        return Math.abs((obs.y + obs.height) - enemyBottom) < 10f;
    }

    // Verifica se há linha de visão livre entre dois pontos, ignorando obstáculos de chão
    private boolean hasLineOfSight(Vector2 from, Vector2 to, Rectangle[] obstacles) {
        for (Rectangle obs : obstacles) {
            if (isGroundObstacle(obs)) continue;
            if (lineIntersectsRectangle(from, to, obs)) {
                return false;
            }
        }
        return true;
    }

    // Retorna true se o segmento entre p1 e p2 intersecta o retângulo rect
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

    // Testa se os segmentos (p1-p2) e (p3-p4) se intersectam.
    private boolean linesIntersect(Vector2 p1, Vector2 p2, Vector2 p3, Vector2 p4) {
        float denom = (p2.x - p1.x) * (p4.y - p3.y) - (p2.y - p1.y) * (p4.x - p3.x);
        if (Math.abs(denom) < 0.0001f) return false;
        float ua = ((p4.x - p3.x) * (p1.y - p3.y) - (p4.y - p3.y) * (p1.x - p3.x)) / denom;
        float ub = ((p2.x - p1.x) * (p1.y - p3.y) - (p2.y - p1.y) * (p1.x - p3.x)) / denom;
        return (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1);
    }
}
