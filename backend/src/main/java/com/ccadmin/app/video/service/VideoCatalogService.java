package com.ccadmin.app.video.service;

import com.ccadmin.app.shared.model.dto.ResponsePageSearchT;
import com.ccadmin.app.shared.service.SessionService;
import com.ccadmin.app.video.model.entity.ActorEntity;
import com.ccadmin.app.video.model.entity.TagEntity;
import com.ccadmin.app.video.model.entity.VideoCategoryEntity;
import com.ccadmin.app.video.repository.ActorRepository;
import com.ccadmin.app.video.repository.TagRepository;
import com.ccadmin.app.video.repository.VideoCategoryRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VideoCatalogService extends SessionService {
    private static final Logger log = LogManager.getLogger(VideoCatalogService.class);

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
    public List<VideoCategoryEntity> findTopViewedCategories(Integer limit) { return categoryRepository.findTopViewed(limit == null || limit < 1 ? 8 : Math.min(limit, 30)); }
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
        log.info("Iniciando guardado de categoria. categoryCod={}, name={}", entity == null ? "" : entity.CategoryCod, entity == null ? "" : entity.Name);
        if (entity.CategoryCod == null || entity.CategoryCod.isBlank()) {
            entity.CategoryCod = codGeneratorService.next("CAT");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        VideoCategoryEntity saved = categoryRepository.save(entity.validate());
        log.info("Categoria guardada correctamente. categoryCod={}", saved.CategoryCod);
        return saved;
    }

    @Transactional
    public ActorEntity saveActor(ActorEntity entity) {
        log.info("Iniciando guardado de actor. actorCod={}, name={}", entity == null ? "" : entity.ActorCod, entity == null ? "" : entity.Name);
        if (entity.ActorCod == null || entity.ActorCod.isBlank()) {
            entity.ActorCod = codGeneratorService.next("ACT");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        ActorEntity saved = actorRepository.save(entity.validate());
        log.info("Actor guardado correctamente. actorCod={}", saved.ActorCod);
        return saved;
    }

    @Transactional
    public TagEntity saveTag(TagEntity entity) {
        log.info("Iniciando guardado de tag. tagCod={}, name={}", entity == null ? "" : entity.TagCod, entity == null ? "" : entity.Name);
        if (entity.TagCod == null || entity.TagCod.isBlank()) {
            entity.TagCod = codGeneratorService.next("TAG");
            entity.addSessionCreate(getUserCod());
        } else {
            entity.addSessionModify(getUserCod());
        }
        TagEntity saved = tagRepository.save(entity.validate());
        log.info("Tag guardado correctamente. tagCod={}", saved.TagCod);
        return saved;
    }

    @Transactional
    public VideoCategoryEntity enableCategory(String cod) {
        log.info("Iniciando activacion de categoria. categoryCod={}", cod);
        VideoCategoryEntity e = categoryRepository.findById(cod).orElseThrow();
        e.active(getUserCod());
        VideoCategoryEntity saved = categoryRepository.save(e);
        log.info("Categoria activada correctamente. categoryCod={}", saved.CategoryCod);
        return saved;
    }

    @Transactional
    public VideoCategoryEntity disableCategory(String cod) {
        log.info("Iniciando desactivacion de categoria. categoryCod={}", cod);
        VideoCategoryEntity e = categoryRepository.findById(cod).orElseThrow();
        e.inactive(getUserCod());
        VideoCategoryEntity saved = categoryRepository.save(e);
        log.info("Categoria desactivada correctamente. categoryCod={}", saved.CategoryCod);
        return saved;
    }

    @Transactional
    public ActorEntity enableActor(String cod) {
        log.info("Iniciando activacion de actor. actorCod={}", cod);
        ActorEntity e = actorRepository.findById(cod).orElseThrow();
        e.active(getUserCod());
        ActorEntity saved = actorRepository.save(e);
        log.info("Actor activado correctamente. actorCod={}", saved.ActorCod);
        return saved;
    }

    @Transactional
    public ActorEntity disableActor(String cod) {
        log.info("Iniciando desactivacion de actor. actorCod={}", cod);
        ActorEntity e = actorRepository.findById(cod).orElseThrow();
        e.inactive(getUserCod());
        ActorEntity saved = actorRepository.save(e);
        log.info("Actor desactivado correctamente. actorCod={}", saved.ActorCod);
        return saved;
    }

    @Transactional
    public TagEntity enableTag(String cod) {
        log.info("Iniciando activacion de tag. tagCod={}", cod);
        TagEntity e = tagRepository.findById(cod).orElseThrow();
        e.active(getUserCod());
        TagEntity saved = tagRepository.save(e);
        log.info("Tag activado correctamente. tagCod={}", saved.TagCod);
        return saved;
    }

    @Transactional
    public TagEntity disableTag(String cod) {
        log.info("Iniciando desactivacion de tag. tagCod={}", cod);
        TagEntity e = tagRepository.findById(cod).orElseThrow();
        e.inactive(getUserCod());
        TagEntity saved = tagRepository.save(e);
        log.info("Tag desactivado correctamente. tagCod={}", saved.TagCod);
        return saved;
    }
}
