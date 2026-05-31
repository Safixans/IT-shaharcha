package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.RoadmapCardResponse;
import com.itshaharcha.learning.dto.response.RoadmapDetailResponse;
import com.itshaharcha.learning.dto.response.RoadmapEdgeResponse;
import com.itshaharcha.learning.dto.response.RoadmapNodeResponse;
import com.itshaharcha.learning.entity.Course;
import com.itshaharcha.learning.entity.Roadmap;
import com.itshaharcha.learning.entity.RoadmapEdge;
import com.itshaharcha.learning.entity.RoadmapNode;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.RoadmapEdgeRepository;
import com.itshaharcha.learning.repository.RoadmapNodeRepository;
import com.itshaharcha.learning.repository.RoadmapRepository;
import com.itshaharcha.learning.service.RoadmapService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapNodeRepository nodeRepository;
    private final RoadmapEdgeRepository edgeRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoadmapCardResponse> listRoadmaps(String q, String kind, Pageable pageable) {
        String query = StringUtils.hasText(q) ? q : null;
        String kindFilter = StringUtils.hasText(kind) ? kind : null;
        return PageResponse.from(
                roadmapRepository.searchPublished(query, kindFilter, pageable),
                r -> new RoadmapCardResponse(
                        r.getId(), r.getSlug(), r.getTitle(), r.getTagline(), r.getIcon(),
                        r.getKind(), r.getDifficulty(),
                        (int) nodeRepository.countByRoadmapId(r.getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public RoadmapDetailResponse getRoadmap(String slug) {
        Roadmap roadmap = roadmapRepository.findBySlug(slug)
                .filter(r -> "published".equals(r.getStatus()))
                .orElseThrow(() -> ApplicationException.notFound("Roadmap not found"));

        List<RoadmapNode> nodes = nodeRepository.findByRoadmapIdOrderByOrderIndexAsc(roadmap.getId());

        Map<UUID, String> courseTitles = courseTitlesFor(nodes);
        List<RoadmapNodeResponse> nodeResponses = nodes.stream()
                .map(n -> new RoadmapNodeResponse(
                        n.getNodeKey(), n.getType(), n.getTitle(), n.getSummary(), n.getDetail(),
                        n.isOptional(), n.getOrderIndex(), n.getPosX(), n.getPosY(),
                        n.getCourseId(),
                        n.getCourseId() == null ? null : courseTitles.get(n.getCourseId())))
                .toList();

        Map<UUID, String> keyById = nodes.stream()
                .collect(Collectors.toMap(RoadmapNode::getId, RoadmapNode::getNodeKey));
        List<RoadmapEdgeResponse> edgeResponses = edgeRepository.findByRoadmapId(roadmap.getId()).stream()
                .filter(e -> keyById.containsKey(e.getFromNodeId()) && keyById.containsKey(e.getToNodeId()))
                .map(e -> new RoadmapEdgeResponse(
                        keyById.get(e.getFromNodeId()), keyById.get(e.getToNodeId()),
                        e.getKind(), e.getStyle()))
                .toList();

        return new RoadmapDetailResponse(
                roadmap.getId(), roadmap.getSlug(), roadmap.getTitle(), roadmap.getTagline(),
                roadmap.getDescription(), roadmap.getIcon(), roadmap.getKind(), roadmap.getDifficulty(),
                roadmap.getLayoutMode(), nodeResponses, edgeResponses);
    }

    private Map<UUID, String> courseTitlesFor(List<RoadmapNode> nodes) {
        List<UUID> courseIds = nodes.stream()
                .map(RoadmapNode::getCourseId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, String> titles = new HashMap<>();
        for (Course c : courseRepository.findAllById(courseIds)) {
            titles.put(c.getId(), c.getTitle());
        }
        return titles;
    }
}
