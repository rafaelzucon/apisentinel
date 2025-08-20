# ApiSentinel — Hub de Governança de APIs

## 📌 Visão Geral
O **ApiSentinel** é um **hub de governança de APIs**, projetado para automatizar e padronizar a forma como ativos de APIs são descobertos, normalizados e sincronizados com o catálogo corporativo de Governança.

Ele conecta múltiplas fontes (**ExternalApi API Manager**, **CSV manual** e **GitLab** como fallback) e garante que todas as APIs sejam publicadas ou atualizadas no catálogo com **dados completos e consistentes**.

---

## 🚀 Funcionalidades
- **Descoberta automática** de APIs registradas no **ExternalApi API Manager**.  
- **Fallback com GitLab**: se versão ou contexto estiverem ausentes, o sistema busca em repositórios GitLab (namespace e tags).  
- **Upload manual via endpoint**: envio de `data/input.csv` dispara todo o pipeline automaticamente.  
- **Sincronização com Governança**: criação/atualização de ativos e associações entre APIs internas e expostas.  
- **Execução sob demanda ou agendada (cron)**.  
- **Relatórios automáticos**:  
  - `discovery_log`: operações realizadas (CREATED/UPDATED/ASSOCIATION_CREATED/EXPOSED_ASSET_NOT_FOUND), com fonte (CSV/SENSEDIA).
  - `inconsistency_log`: inconsistências detectadas (ex.: NAME_REQUIRED, VERSION_REQUIRED).
- **Persistência em DB**:
  - `asset_gw`: espelho dos ativos descobertos.
  - `discovery_log`
  - `inconsistency_log`
  - 
---

## 💡 Valor para o Negócio
- **Confiança nos dados**: garante que APIs cheguem ao catálogo com versão e contexto corretos.  
- **Autonomia dos squads**: upload de CSV dispara o pipeline na hora, sem depender apenas de agendamentos.  
- **Eficiência operacional**: elimina retrabalhos manuais na governança.  
- **Escalabilidade**: suporta crescimento do portfólio de APIs sem inflar equipes de governança.  
- **Base para monetização**: catálogo consistente e confiável facilita a exposição de APIs como produtos.  

---

### 3. Time-to-Market
- Novas APIs publicadas no **mesmo dia** (via upload ou sync) e com **metadados completos**.  
- Lead time de publicação: **minutos**.  

### 4. Indicadores de Valor (KPIs)
- % de ativos com dados completos já na primeira ingestão.  
- Tempo médio entre criação e publicação no catálogo.  
- Inconsistências corrigidas automaticamente vs inconsistências manuais.  
- Horas de governança economizadas.  

---

## 🛠️ Tecnologias
- Spring Boot 3.x, Java 21, Spring Batch, WebFlux (WebClient), Spring Data JPA.
- H2/MariaDB.
- Integrações: ExternalApi API Manager, GitLab, Catálogo de Governança.


## Diagramas e Fluxos






