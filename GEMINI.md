# Diretrizes de Projeto: Mods de Minecraft (Leandro / Rota Mods)

## 1. Padrão de Versionamento e Segurança de Código
- **Commits e Tags de Marco**: Sempre que uma versão ou funcionalidade for concluída e validada pelo usuário, ela deve ser imediatamente comitada no Git local e etiquetada com uma Tag semântica (ex: `v1.0.0`, `v1.1.0`).
- **Preservação de Código Estável**: Nunca refazer ou apagar algoritmos funcionais sem criar uma nova branch ou garantir que a versão estável anterior esteja 100% preservada e recuperável via Git.

## 2. Repositório Central de Distribuição e Publicação
Todo mod desenvolvido deve manter sua pasta oficial de distribuição atualizada em:
`/home/leo/Documentos/projetos minecraft/MODS/<Nome do Mod>/`

Estrutura obrigatória por mod:
```text
/home/leo/Documentos/projetos minecraft/MODS/<Nome do Mod>/
├── README.md                          # Visão geral, requisitos, comandos e funcionamento
├── CURSEFORGE_DESCRIPTION.md          # Página formatada em Markdown/HTML pronta para o CurseForge
├── docs/
│   ├── ARCHITECTURE.md                # Arquitetura interna, fluxo de dados e classes principais
│   └── CONFIGURATION_GUIDE.md         # Documentação de todas as opções de configuração
└── versions/
    └── v<X.Y.Z>/
        ├── <mod_id>-<X.Y.Z>.jar       # Arquivo compilado e testado pronto para publicação
        ├── CHANGELOG.md               # Notas detalhadas da versão e correções
        └── RELEASE_INFO.json          # Metadados JSON (versões, loader, checksum SHA256)
```

## 3. Diretrizes de Engenharia para o Mod "Rota Roads: Shaper"
- **Geração Pré-Jogo e Desempenho**:
  - Toda a malha inicial do spawn e conexões entre vilas deve ter seus traçados (waypoints) calculados na inicialização do nível (`LevelEvent.Load`).
  - A materialização física nos chunks de spawn ocorre nativamente no `LevelLoadingScreen` ("0% ... 100%") via `RoadCarverFeature` (step `surface_structures`).
  - **PROIBIDO** chamar `serverLevel.getChunkSource().getChunk(..., FULL, true)` de forma bloqueante na thread principal para chunks distantes fora do spawn. Utilizar `getChunkNow` para chunks em memória e deixar chunks distantes serem esculpidos de forma limpa quando forem gerados ou carregados.
- **Pontes e Corpos d'Água**:
  - Altura padrão de pontes sobre água: exatamente 2 blocos acima do nível d'água (`waterBridgeHeightAboveWater`).
  - Poças e valas rasas (profundidade $< 2$ ou largura $< 5$ blocos): **não** constroem pontes; recebem aterro sólido e compactado com pavimento normal.
  - Córregos e rios: pontes de madeira (deck de `oak_planks`, bordas e pilares de `oak_log`, para-peito de `oak_fence`).
  - Lagos maiores e mares: pontes de pedra (deck de `deepslate_tiles`, bordas e pilares de `stone_bricks`, para-peito de `stone_brick_wall`).
  - Largura da ponte: deck igual à largura da estrada, com $+1$ bloco de cada lado para as bordas e para-peito (largura total = estrada + 2).
- **Subidas e Descidas (Rampas de Lajes)**:
  - Progressão estrita de degraus de 0.5 blocos: `Bloco Sólido (x) -> Laje (x.5) -> Bloco Sólido (x+1) -> Laje (x+1.5)`.
  - Nunca colocar laje sobre bloco que já subiu em relação ao ponto anterior (`isStepUp` e `isStepDown` só disparam se `wp_anterior.getY() == wp_atual.getY()`).
- **Iluminação e Ausência de Sombras Artificiais**:
  - Ao remover blocos sólidos para abrir o gabarito livre de passagem da estrada (ar), sempre acionar `serverLevel.getChunkSource().getLightEngine().checkBlock(airPos)` para atualizar o skylight e eliminar sombras pretas.
- **Proteção de Vilas**:
  - O traçado A* deve contornar e nunca destruir plantações (`farmland`, `crops`), fazendas, composteiras ou casas de aldeões, ancorando nas ruas periféricas externas das vilas.

## 4. Filosofia Arquitetural do "Rota Core" e Otimizações Compartilhadas
- **Princípio de Centralização no Core**: Sempre que uma funcionalidade, utilitário, despachante ou otimização for de propósito geral ou de infraestrutura de mundo (não exclusiva de um único mod como estradas ou missões), ela deve residir no `Rota Core` para que toda a suíte de mods herde a mesma estabilidade e alta performance.
- **Leveza Extrema e Sobrecarga Zero (Zero Overhead)**:
  - O `Rota Core` deve ser ultra leve e modular.
  - Se o jogador estiver utilizando apenas o `Rota Core` e um único outro mod (ex: apenas *Rota Roads* ou apenas *Rota Questbook*), o Core **não deve** executar loops em segundo plano, alocar caches desnecessários ou consumir recursos por subsistemas que nenhum mod ativo está usando.
  - Otimizações no Core devem ser utilitárias e orientadas a demanda (*on-demand*):
    - *Despachante de Iluminação*: Evita travar a fila do `ThreadedLevelLightEngine` na geração de features e só aciona `checkBlock` em chunks carregados.
    - *Acesso Não-Bloqueante a Chunks*: Helpers padronizados (`getChunkNow`) para inspecionar blocos e estruturas sem congelar a thread principal.
    - *Caches de Relevo e Altura*: Amostragem rápida de superfície com máscaras bitwise `(x & 15) | ((z & 15) << 4)` e limite seguro de varredura vertical.
    - *Executor Assíncrono Único*: Pool de threads global compartilhado de baixa prioridade, evitando que múltiplos mods saturem os núcleos da CPU.
- **Fronteira Rígida de Responsabilidade**:
  - **No Rota Core**: A infraestrutura, os motores de leitura segura, widgets visuais, zoneamento regional macro e otimizações gerais.
  - **Nos Mods Satélites (Roads, Quests, etc.)**: Apenas regras de negócio específicas, inteligência algorítmica temática (ex: A* com gradiente para vias), elementos estéticos próprios e fluxo de jogabilidade.
