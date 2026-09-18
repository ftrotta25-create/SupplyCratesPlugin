# SupplyCratesPlugin

Plugin standalone para Paper **1.20.1**. Supply Crates que aparecen
random en el mapa cada cierto tiempo, con loot variado pensado para el
arranque del server (nada de netherite ni diamante encantado).

## Cómo funciona

- Cada `intervaloMinutos` (default 20) el plugin intenta spawnear una
  crate nueva en un punto random dentro de `radio` bloques del centro
  configurado, siempre y cuando no se haya llegado a
  `crateSimultaneasMax` crates activas al mismo tiempo.
- La crate es un cofre normal con `itemsPorCrate` items random adentro,
  con una columna de partículas (`END_ROD`) marcando dónde está.
- Se anuncia por server con las coordenadas exactas apenas aparece.
- Si nadie la abre en `tiempoExpiracionMinutos`, desaparece sola
  (tirando el contenido al piso, no se pierde nada).
- Si un jugador la vacía del todo, el cofre desaparece al cerrar el
  inventario (libera el cupo antes de la expiración natural).

## Comandos

- `/supplycrate spawn` — fuerza el spawn de una crate ya, en un punto
  random del mapa (admin, para testear o para eventos puntuales).
- `/supplycrate aqui` — spawnea una crate justo donde estás parado
  (esta sí ignora `radio`/`centroX`/`centroZ`, va exactamente a tu
  ubicación). Cuenta para el límite de `crateSimultaneasMax` igual que
  las automáticas.
- `/supplycrate lista` — cuántas crates hay activas ahora mismo.

## Loot

Tres niveles de rareza (pesos relativos, no hace falta que sumen 100):

| Rareza | Peso | Contenido |
|---|---|---|
| Común | 20-35 | Pan, manzana, hierro en bruto, madera, flechas, antorchas, carbón, cuerda |
| Poco común | 8-12 | Oro, esmeralda, zanahoria dorada, herramienta de hierro random |
| Rara | 3-4 | Diamante, manzana dorada |

**Si tenés MateSystemPlugin instalado**, se suman automáticamente:

| Rareza | Peso | Contenido |
|---|---|---|
| Poco común | 12 | Una hierba random (Cedrón/Poleo/Boldo/Azúcar) |
| Poco común | 8 | Un Mate vacío |
| Rara | 3 | Una Pava de Hierro Encantada ya craftada entera |

Son los ítems REALES de MateSystemPlugin (no una copia) — se
reconocen perfecto por ese plugin (cebar mate, recargar la pava, todo
funciona igual que si los hubieras crafteado).

Si no tenés MateSystemPlugin instalado, el plugin lo detecta solo y
simplemente no incluye esos items — no hace falta ninguna
configuración para eso.

## Ajustar a gusto

- Todos los pesos y cantidades están en `LootTableFactory.java`,
  agrupados por rareza — fácil de sumar, sacar o rebalancear items.
- Los tiempos e intervalos están en `config.yml`.

## Nota técnica: cómo compila esto

`MateSystemPlugin` no está publicado en ningún repositorio Maven
público, así que el workflow de GitHub Actions (`.github/workflows/build.yml`)
primero clona ese repo, le hace `mvn install` (dejándolo en el cache
local del runner), y RECIÉN AHÍ compila este plugin, que lo toma como
dependencia normal. Si en algún momento cambia el nombre del repo de
MateSystemPlugin o su versión en el `pom.xml`, hay que actualizar esa
referencia acá también.

## Ideas para más adelante

- Integrar de la misma forma a JefeCabraPlugin (Cuerno de Cabra
  Maldito como loot raro) — mismo patrón: exponer un
  `getItemFactory()` público y agregarlo al `LootTableFactory`.
- Anuncio con título/sonido más dramático en vez de solo el mensaje de
  chat.
- Protección temporal de la crate (que no la puedan romper con TNT
  para saltarse el "primero en llegar").
