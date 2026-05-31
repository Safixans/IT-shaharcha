package com.itshaharcha.learning.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itshaharcha.learning.entity.Roadmap;
import com.itshaharcha.learning.entity.RoadmapEdge;
import com.itshaharcha.learning.entity.RoadmapNode;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.RoadmapEdgeRepository;
import com.itshaharcha.learning.repository.RoadmapNodeRepository;
import com.itshaharcha.learning.repository.RoadmapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Seeds the four original roadmaps from {@code seed/roadmaps-seed.json} on first
 * boot (when the roadmaps table is empty). Node keys are preserved so any
 * progress already stored in the browser stays valid. Idempotent: skips if any
 * roadmap already exists.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoadmapSeeder implements ApplicationRunner {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapNodeRepository nodeRepository;
    private final RoadmapEdgeRepository edgeRepository;
    private final CourseRepository courseRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (roadmapRepository.count() > 0) {
            return;
        }
        List<SeedRoadmap> seeds = readSeeds();
        for (SeedRoadmap seed : seeds) {
            seedRoadmap(seed);
        }
        log.info("Seeded {} roadmaps", seeds.size());
    }

    private List<SeedRoadmap> readSeeds() throws Exception {
        try (InputStream in = new ClassPathResource("seed/roadmaps-seed.json").getInputStream()) {
            return objectMapper.readValue(in, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, SeedRoadmap.class));
        }
    }

    private void seedRoadmap(SeedRoadmap seed) {
        Roadmap roadmap = new Roadmap();
        roadmap.setSlug(seed.slug());
        roadmap.setTitle(seed.title());
        roadmap.setTagline(seed.tagline());
        roadmap.setDescription(seed.description());
        roadmap.setIcon(seed.icon());
        roadmap.setKind("role");
        roadmap.setStatus("published");
        roadmap.setLayoutMode("auto");
        roadmap.setDifficulty(seed.difficulty());
        roadmap.setPublishedAt(Instant.now());
        roadmap = roadmapRepository.save(roadmap);

        UUID lastSpineNodeId = null;
        int order = 0;
        for (SeedStep step : seed.steps()) {
            RoadmapNode node = new RoadmapNode();
            node.setRoadmapId(roadmap.getId());
            node.setNodeKey(step.id());
            node.setType(step.milestone() ? "milestone" : "topic");
            node.setTitle(step.title());
            node.setSummary(step.summary());
            node.setDetail(step.detail());
            node.setOptional(step.optional());
            node.setOrderIndex(order++);
            if (step.courseTitle() != null) {
                courseRepository.findFirstByTitleIgnoreCase(step.courseTitle())
                        .ifPresent(c -> node.setCourseId(c.getId()));
            }
            RoadmapNode saved = nodeRepository.save(node);

            if (step.optional()) {
                if (lastSpineNodeId != null) {
                    createEdge(roadmap.getId(), lastSpineNodeId, saved.getId(), "branch", "dotted");
                }
            } else {
                if (lastSpineNodeId != null) {
                    createEdge(roadmap.getId(), lastSpineNodeId, saved.getId(), "sequence", "solid");
                }
                lastSpineNodeId = saved.getId();
            }
        }
    }

    private void createEdge(UUID roadmapId, UUID from, UUID to, String kind, String style) {
        RoadmapEdge edge = new RoadmapEdge();
        edge.setRoadmapId(roadmapId);
        edge.setFromNodeId(from);
        edge.setToNodeId(to);
        edge.setKind(kind);
        edge.setStyle(style);
        edgeRepository.save(edge);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SeedRoadmap(
            String slug, String title, String icon, String tagline,
            String description, String difficulty, List<SeedStep> steps) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record SeedStep(
            String id, String title, String summary, String detail,
            String courseTitle, boolean optional, boolean milestone) {
    }
}
