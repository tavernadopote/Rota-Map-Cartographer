# Rota Map: Cartographer 🗺️

**Rota Map: Cartographer** é um mod cartográfico diegético e técnico para Minecraft (NeoForge 1.21.1) pertencente à suíte **Rota Mods**.

Ele introduz uma **Carta Topográfica Cartográfica** autêntica desenhada diretamente sobre o relevo procedural da Seed do mundo, utilizando amostragem matemática assíncrona e o algoritmo de **Marching Squares (Quadrados Marchantes)**.

---

## ✨ Recursos Principais

- **Curvas de Nível Matemáticas (Marching Squares)**:
  - Amostragem contínua via equações de ruído do Minecraft (`getBaseHeight`) sem carregar blocos nem causar engasgos (*stuttering*).
  - Interpolação linear precisa delimitando variações de altitude, montanhas, planícies e linhas de costa aquática ($Y=63$).
- **Generalização Cartográfica Inteligente (LOD Dinâmico)**:
  - **Zoom Próximo**: curvas a cada 10 metros, revelando pequenos vales e depressões.
  - **Zoom Médio**: curvas a cada 25 metros, mantendo o mapa arejado e legível.
  - **Zoom Panorâmico**: apenas Curvas Mestras (a cada 50 metros) e costa marítima, eliminando o emaranhado de linhas em montanhas íngremes.
- **Anti-Aliasing de Baixa Resolução**:
  - Descarte automático de microsegmentos sub-pixel para eliminar serrilhados e dentes de serra.
- **Integração com Rota Roads: Shaper**:
  - Quando instalado em conjunto com o *Rota Roads*, desenha automaticamente a malha regional de vilas ('X' e casinhas) e as estradas pavimentadas, rurais e túneis.
- **Visual Cartográfico Autêntico**:
  - Fundo de papel pergaminho rústico, agulha de bússola do jogador com coordenadas em tempo real, régua de escala métrica e leitura de elevação sob o cursor.
- **Zero Blur**:
  - Tela 100% nítida, sem pós-processamento de desfoque ou sombreamento escuro do menu padrão do Minecraft.

---

## ⌨️ Controles e Comandos

- **Abrir/Fechar o Mapa**: Pressione a tecla **`M`** (configurável nas opções de controles).
- **Comando Direto**: Digite `/rotamap` no chat.
- **Pelo Rota Hub**: Abra o menu unificado da tecla **`O`** e clique na aba *Rota Map: Cartographer*.
- **Navegação**: Arraste com o mouse para mover a visualização; use a rodinha do mouse ou os botões `[+]` e `[-]` para controlar o zoom.

---

## 📦 Requisitos

- **Minecraft:** `1.21.1`
- **Mod Loader:** `NeoForge >= 21.1.65`
- **Biblioteca Base:** `Rota Core >= 1.1.0`
- **Java:** `21`

---

## 📜 Licença
Distribuído sob a licença MIT. Desenvolvido por Leandro / Rota Mods.
