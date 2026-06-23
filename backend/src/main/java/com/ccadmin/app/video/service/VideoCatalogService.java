package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.entity.ActorEntity;
import com.ccadmin.app.video.model.entity.TagEntity;
import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import com.ccadmin.app.video.repository.ActorRepository;
import com.ccadmin.app.video.repository.TagRepository;
import com.ccadmin.app.video.repository.VideoCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VideoCatalogService extends SessionService {
    private final VideoCategoryRepository categoryRepository;
    private final ActorRepository actorRepository;
    private final TagRepository tagRepository;
    private final CodGeneratorService codGeneratorService;

    public VideoCatalogService(VideoCategoryRepository categoryRepository, ActorRepository actorRepository, TagRepository tagRepository, CodGeneratorService codGeneratorService) {
        this.categoryRepository = categoryRepository;
        this.actorRepository = actorRepository;
        this.tagRepository = tagRepository;
        this.codGeneratorService = codGeneratorService;
    }

    public List<VideoCategoryEntity> findActiveCategories() { return categoryRepository.findActives(); }
    public List<ActorEntity> findActiveActors() { return actorRepository.findActives(); }
    public List<TagEntity> findActiveTags() { return tagRepository.findActives(); }

    public ResponsePageSearchT<VideoCategoryEntity> findCategories(String query, String status, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : limit;
        String q = query == null ? "" : query;
        String s = status == null ? "" : status;
        return new ResponsePageSearchT<>(categoryRepository.findByFilters(q, s, (safePage - 1) * safeLimit, safeLimit), categoryRepository.countByFilters(q, s), safePage, safeLimit);
    }

    public ResponsePageSearchT<ActorEntity> findActors(String query, String status, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : limit;
        String q = query == null ? "" : query;
        String s = status == null ? "" : status;
        return new ResponsePageSearchT<>(actorRepository.findByFilters(q, s, (safePage - 1) * safeLimit, safeLimit), actorRepository.countByFilters(q, s), safePage, safeLimit);
    }

    public ResponsePageSearchT<TagEntity> findTags(String query, String status, Integer page, Integer limit) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safeLimit = limit == null || limit < 1 ? 10 : limit;
        String q = query == null ? "" : query;
        String s = status == null ? "" : status;
        return new ResponsePageSearchT<>(tagRepository.findByFilters(q, s, (safePage - 1) * safeLimit, safeLimit), tagRepository.countByFilters(q, s), safePage, safeLimit);
    }

    @Transactional
    public VideoCategoryEntity saveCategory(VideoCategoryEntity entity) {
        if (entity.CategoryCod == null || entity.CategoryCod.isBlank()) {
            entity.CategoryCod = codGeneratorService.next("CAT");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        return categoryRepository.save(entity.validate());
    }

    @Transactional
    public ActorEntity saveActor(ActorEntity entity) {
        if (entity.ActorCod == null || entity.ActorCod.isBlank()) {
            entity.ActorCod = codGeneratorService.next("ACT");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        return actorRepository.save(entity.validate());
    }

    @Transactional
    public TagEntity saveTag(TagEntity entity) {
        if (entity.TagCod == null || entity.TagCod.isBlank()) {
            entity.TagCod = codGeneratorService.next("TAG");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        return tagRepository.save(entity.validate());
    }

    @Transactional public VideoCategoryEntity enableCategory(String cod) { VideoCategoryEntity e = categoryRepository.findById(cod).orElseThrow(); e.active(getUserCod()); return categoryRepository.save(e); }
    @Transactional public VideoCategoryEntity disableCategory(String cod) { VideoCategoryEntity e = categoryRepository.findById(cod).orElseThrow(); e.inactive(getUserCod()); return categoryRepository.save(e); }
    @Transactional public ActorEntity enableActor(String cod) { ActorEntity e = actorRepository.findById(cod).orElseThrow(); e.active(getUserCod()); return actorRepository.save(e); }
    @Transactional public ActorEntity disableActor(String cod) { ActorEntity e = actorRepository.findById(cod).orElseThrow(); e.inactive(getUserCod()); return actorRepository.save(e); }
    @Transactional public TagEntity enableTag(String cod) { TagEntity e = tagRepository.findById(cod).orElseThrow(); e.active(getUserCod()); return tagRepository.save(e); }
    @Transactional public TagEntity disableTag(String cod) { TagEntity e = tagRepository.findById(cod).orElseThrow(); e.inactive(getUserCod()); return tagRepository.save(e); }
}
