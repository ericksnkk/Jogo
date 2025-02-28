package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class BossGolem extends Inimigo {
    // Máquina de estados refinada
    public enum State {
        IDLE,              // Fora do detectionRange
        MOVING,            // Persegue o player
        MELEE_PUNCH,       // Realiza um soco
        MELEE_COOLDOWN,    // Pequeno intervalo entre socos
        LASER_CHARGE,      // Tempo de carga do laser (1s)
        LASER_FIRE,        // Dispara o feixe laser (3s)
        ROCKFALL_ATTACK    // Derruba pedras (3s)
    }

    private State state;
    private float stateTimer;  // Tempo acumulado no estado atual

    // Ranges (em pixels)
    private final float detectionRange = 800f;      // Para localizar o player
    private final float rangedAttackRange = 600f;     // Para ataques à distância
    private final float meleeRange = 150f;            // Para ataque corpo a corpo

    // Cooldowns (em segundos)
    private float laserCooldown;
    private float rockfallCooldown;
    private final float LASER_COOLDOWN = 45f;
    private final float ROCKFALL_COOLDOWN = 30f;

    // Durações dos estados/ataques
    private final float MELEE_PUNCH_DURATION = 0.3f;
    private final float MELEE_COOLDOWN_DURATION = 0.5f;
    private final float LASER_CHARGE_DURATION = 1.0f;  // Tempo de carga antes do laser
    private final float LASER_FIRE_DURATION = 3.0f;      // Tempo efetivo do laser
    private final float ROCKFALL_DURATION = 3.0f;          // Duração do ataque de derrubada de pedras

    // Danos dos ataques
    private final int MELEE_DAMAGE = 5;
    private final int LASER_DAMAGE = 3;
    private final int ROCK_DAMAGE = 10;  // Dano de cada pedra

    // Velocidade de movimentação
    private final float moveSpeed = 80f;

    // Parâmetros para o feixe laser
    private final float LASER_LENGTH = 400f;
    private final float LASER_THICKNESS = 10f;

    // Parâmetros para a hitbox do ataque melee
    private final float MELEE_DAMAGE_AREA_WIDTH = 50f;
    private final float MELEE_DAMAGE_AREA_HEIGHT_MULTIPLIER = 0.4f;

    // Lógica interna para o ataque ROCKFALL (derrubada de pedras)
    // Cada pedra é representada por um retângulo que cai verticalmente
    private Array<Rectangle> fallingRocks;
    private float rockSpawnTimer;
    private final float ROCK_SPAWN_INTERVAL = 0.5f;
    private final float rockFallSpeed = 300f;

    // Flag de debug para desenhar áreas e hitboxes
    private boolean debugMode = true;

    /**
     * Construtor do BossGolem.
     *
     * @param texture  Textura principal do boss.
     * @param startX   Posição X inicial.
     * @param startY   Posição Y inicial.
     */
    public BossGolem(Texture texture, float startX, float startY) {
        // Cria o boss com escala 1.0f (tamanho reduzido), velocidade moveSpeed e vida 50.
        super(texture, startX, startY, 1.0f, 80.0f, 50);
        state = State.MOVING;
        stateTimer = 0f;
        laserCooldown = 0f;
        rockfallCooldown = 0f;
        rockSpawnTimer = 0f;
        fallingRocks = new Array<Rectangle>();
    }

    /**
     * Retorna o centro do boss com base na posição e escala do sprite.
     */
    public Vector2 getCenterPosition() {
        return new Vector2(sprite.getX() + sprite.getWidth() * scale / 2,
                sprite.getY() + sprite.getHeight() * scale / 2);
    }

    @Override
    public void update(float delta, Player player, Rectangle[] obstacles, Rectangle cameraBounds) {
        if (!isAlive) return;
        stateTimer += delta;
        if (laserCooldown > 0) laserCooldown -= delta;
        if (rockfallCooldown > 0) rockfallCooldown -= delta;

        Vector2 bossCenter = getCenterPosition();
        Vector2 playerCenter = player.getCenterPosition();
        float distance = bossCenter.dst(playerCenter);

        // Seleção de estado – somente se o boss não estiver em meio a um ataque (punch, laser ou rockfall)
        if (state == State.MOVING || state == State.IDLE) {
            if (distance > detectionRange) {
                state = State.IDLE;
            } else {
                if (distance <= meleeRange) {
                    state = State.MELEE_PUNCH;
                    stateTimer = 0f;
                } else if (distance <= rangedAttackRange) {
                    if (laserCooldown <= 0) {
                        state = State.LASER_CHARGE;
                        stateTimer = 0f;
                    } else if (rockfallCooldown <= 0) {
                        state = State.ROCKFALL_ATTACK;
                        stateTimer = 0f;
                        rockSpawnTimer = 0f;
                        fallingRocks.clear();
                    } else {
                        state = State.MOVING;
                    }
                } else {
                    state = State.MOVING;
                }
            }
        }

        // Processamento dos estados
        switch (state) {
            case IDLE:
                // Sem ação; pode exibir animação idle.
                break;
            case MOVING:
                // Move-se horizontalmente em direção ao player
                if (playerCenter.x < bossCenter.x) {
                    sprite.translateX(-moveSpeed * delta);
                } else {
                    sprite.translateX(moveSpeed * delta);
                }
                break;
            case MELEE_PUNCH:
                performMeleeAttack(player);
                if (stateTimer >= MELEE_PUNCH_DURATION) {
                    state = State.MELEE_COOLDOWN;
                    stateTimer = 0f;
                }
                break;
            case MELEE_COOLDOWN:
                // Durante o cooldown, o boss fica parado; se o player ainda estiver em meleeRange, pode iniciar outro punch.
                if (stateTimer >= MELEE_COOLDOWN_DURATION) {
                    if (playerCenter.dst(bossCenter) <= meleeRange) {
                        state = State.MELEE_PUNCH;
                    } else {
                        state = State.MOVING;
                    }
                    stateTimer = 0f;
                }
                break;
            case LASER_CHARGE:
                // Tempo de carga do laser (1s). Durante esse tempo, o boss pode exibir um efeito de "preparação".
                if (stateTimer >= LASER_CHARGE_DURATION) {
                    state = State.LASER_FIRE;
                    stateTimer = 0f;
                }
                break;
            case LASER_FIRE:
                performLaserAttack(player);
                if (stateTimer >= LASER_FIRE_DURATION) {
                    laserCooldown = LASER_COOLDOWN;
                    state = State.MOVING;
                    stateTimer = 0f;
                }
                break;
            case ROCKFALL_ATTACK:
                performRockfallAttack(delta);
                if (stateTimer >= ROCKFALL_DURATION) {
                    rockfallCooldown = ROCKFALL_COOLDOWN;
                    state = State.MOVING;
                    stateTimer = 0f;
                }
                break;
        }

        updateFallingRocks(delta, player);
        atualizaHitbox();
    }

    /**
     * Executa o ataque melee: cria uma hitbox à esquerda ou à direita do boss, dependendo da posição do player,
     * e aplica dano se houver sobreposição.
     */
    private void performMeleeAttack(Player player) {
        Vector2 bossCenter = getCenterPosition();
        Vector2 playerCenter = player.getCenterPosition();
        float attackX;
        if (playerCenter.x < bossCenter.x) {
            attackX = sprite.getX() - MELEE_DAMAGE_AREA_WIDTH;
        } else {
            attackX = sprite.getX() + sprite.getWidth() * scale;
        }
        float attackY = sprite.getY() + sprite.getHeight() * scale * 0.3f;
        Rectangle meleeHitbox = new Rectangle(attackX, attackY,
                MELEE_DAMAGE_AREA_WIDTH,
                sprite.getHeight() * scale * MELEE_DAMAGE_AREA_HEIGHT_MULTIPLIER);
        if (meleeHitbox.overlaps(player.getPlayerBounds())) {
            // player.decreaseLife(MELEE_DAMAGE);
        }
    }

    /**
     * Executa o ataque de laser: durante LASER_FIRE, dispara o feixe na direção do player e aplica dano se colidir.
     */
    private void performLaserAttack(Player player) {
        Vector2 bossCenter = getCenterPosition();
        Vector2 playerCenter = player.getCenterPosition();
        int direction = (playerCenter.x < bossCenter.x) ? -1 : 1;
        float laserX;
        if (direction == 1) {
            laserX = sprite.getX() + sprite.getWidth() * scale;
        } else {
            laserX = sprite.getX() - LASER_LENGTH;
        }
        float laserY = sprite.getY() + sprite.getHeight() * scale * 0.5f - LASER_THICKNESS / 2;
        Rectangle laserHitbox = new Rectangle(laserX, laserY, LASER_LENGTH, LASER_THICKNESS);
        if (laserHitbox.overlaps(player.getPlayerBounds())) {
            //player.decreaseLife(LASER_DAMAGE);
        }
    }

    /**
     * Durante o ataque ROCKFALL, a cada ROCK_SPAWN_INTERVAL spawn uma pedra (retângulo) que cai verticalmente.
     */
    private void performRockfallAttack(float delta) {
        rockSpawnTimer += delta;
        if (rockSpawnTimer >= ROCK_SPAWN_INTERVAL) {
            Rectangle rock = new Rectangle();
            rock.x = MathUtils.random(0, Gdx.graphics.getWidth() - 50);
            rock.y = Gdx.graphics.getHeight();
            rock.width = 50;
            rock.height = 50;
            fallingRocks.add(rock);
            rockSpawnTimer = 0f;
        }
    }

    /**
     * Atualiza as pedras que caem: move-as para baixo, verifica colisões com o player e remove as que saíram da tela.
     */
    private void updateFallingRocks(float delta, Player player) {
        for (int i = fallingRocks.size - 1; i >= 0; i--) {
            Rectangle rock = fallingRocks.get(i);
            rock.y -= rockFallSpeed * delta;
            if (rock.overlaps(player.getPlayerBounds())) {
                //player.decreaseLife(ROCK_DAMAGE);
                fallingRocks.removeIndex(i);
            } else if (rock.y + rock.height < 0) {
                fallingRocks.removeIndex(i);
            }
        }
    }

    /**
     * Desenha os círculos de detecção para debug:
     * - Círculo ciano: detectionRange.
     * - Círculo azul: rangedAttackRange.
     * - Círculo verde: meleeRange.
     */
    public void drawDetectionCircles(ShapeRenderer shapeRenderer) {
        Vector2 center = getCenterPosition();
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(center.x, center.y, detectionRange);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.circle(center.x, center.y, rangedAttackRange);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(center.x, center.y, meleeRange);
    }

    /**
     * Renderiza os efeitos especiais do boss:
     * - Durante LASER_FIRE, desenha o feixe laser na direção do player.
     * - Desenha as pedras caindo.
     * - Se em MELEE_PUNCH ou MELEE_COOLDOWN, desenha a hitbox de ataque em magenta.
     * Deve ser chamado no método render() da tela após game.batch.end().
     */
    public void renderEffects(ShapeRenderer shapeRenderer, Player player) {
        // Renderiza o feixe laser se em LASER_FIRE
        if (state == State.LASER_FIRE) {
            Vector2 bossCenter = getCenterPosition();
            Vector2 playerCenter = player.getCenterPosition();
            int direction = (playerCenter.x < bossCenter.x) ? -1 : 1;
            float laserX;
            if (direction == 1) {
                laserX = sprite.getX() + sprite.getWidth() * scale;
            } else {
                laserX = sprite.getX() - LASER_LENGTH;
            }
            float laserY = sprite.getY() + sprite.getHeight() * scale * 0.5f - LASER_THICKNESS / 2;
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(laserX, laserY, LASER_LENGTH, LASER_THICKNESS);
        }
        // Renderiza as pedras caindo
        for (Rectangle rock : fallingRocks) {
            shapeRenderer.setColor(Color.GRAY);
            shapeRenderer.rect(rock.x, rock.y, rock.width, rock.height);
        }
        // Se em um estado de melee (punch ou cooldown), desenha a hitbox do ataque em magenta
        if (state == State.MELEE_PUNCH || state == State.MELEE_COOLDOWN) {
            Vector2 bossCenter = getCenterPosition();
            Vector2 playerCenter = player.getCenterPosition();
            float attackX;
            if (playerCenter.x < bossCenter.x) {
                attackX = sprite.getX() - MELEE_DAMAGE_AREA_WIDTH;
            } else {
                attackX = sprite.getX() + sprite.getWidth() * scale;
            }
            float attackY = sprite.getY() + sprite.getHeight() * scale * 0.3f;
            shapeRenderer.setColor(Color.MAGENTA);
            shapeRenderer.rect(attackX, attackY, MELEE_DAMAGE_AREA_WIDTH,
                    sprite.getHeight() * scale * MELEE_DAMAGE_AREA_HEIGHT_MULTIPLIER);
        }
        // Se o debug estiver ativo, desenha também os círculos de detecção
        if (debugMode) {
            drawDetectionCircles(shapeRenderer);
        }
    }
}
