package com.stage.coc.service;

import com.stage.coc.entity.Agent;
import com.stage.coc.entity.BureauControle;
import com.stage.coc.entity.Demande;
import com.stage.coc.repository.AgentRepository;
import com.stage.coc.repository.BureauControleRepository;
import com.stage.coc.repository.DemandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Transactional
public class AffectationService {

    private final BureauControleRepository bureauControleRepository;
    private final AgentRepository agentRepository;
    private final DemandeRepository demandeRepository;

    // Compteur pour l'algorithme cyclique
    private final AtomicInteger bureauCounter = new AtomicInteger(0);

    public void affecterDemande(Demande demande) {
        // 1. Affecter à un bureau de contrôle selon l'algorithme cyclique
        BureauControle bureau = affecterBureauControle();
        demande.setBureauControle(bureau);

        // 2. Affecter à un agent disponible du bureau
        Agent agent = affecterAgent(bureau);
        demande.setAgent(agent);

        // 3. Incrémenter la charge de travail de l'agent
        if (agent != null) {
            agent.setChargeTravail(agent.getChargeTravail() + 1);
            agentRepository.save(agent);
        }

        demandeRepository.save(demande);
    }

    private BureauControle affecterBureauControle() {
        List<BureauControle> bureaux = bureauControleRepository.findAll();
        if (bureaux.isEmpty()) {
            throw new RuntimeException("Aucun bureau de contrôle disponible");
        }

        // Algorithme cyclique
        int index = bureauCounter.getAndIncrement() % bureaux.size();
        return bureaux.get(index);
    }

    private Agent affecterAgent(BureauControle bureau) {
        // Récupérer les agents disponibles du bureau, triés par charge de travail
        List<Agent> agentsDisponibles = agentRepository
                .findAvailableAgentsByBureauControleOrderByWorkload(bureau.getId());

        if (agentsDisponibles.isEmpty()) {
            // Si aucun agent disponible, retourner null ou throw exception
            return null;
        }

        // Retourner l'agent avec la charge de travail la plus faible
        return agentsDisponibles.get(0);
    }

    public void reaffecter(Long demandeId, Long nouvelAgentId) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

        Agent ancienAgent = demande.getAgent();
        Agent nouvelAgent = agentRepository.findById(nouvelAgentId)
                .orElseThrow(() -> new RuntimeException("Agent non trouvé"));

        // Décrémenter la charge de l'ancien agent
        if (ancienAgent != null) {
            ancienAgent.setChargeTravail(Math.max(0, ancienAgent.getChargeTravail() - 1));
            agentRepository.save(ancienAgent);
        }

        // Affecter au nouvel agent
        demande.setAgent(nouvelAgent);
        nouvelAgent.setChargeTravail(nouvelAgent.getChargeTravail() + 1);

        agentRepository.save(nouvelAgent);
        demandeRepository.save(demande);
    }
}

