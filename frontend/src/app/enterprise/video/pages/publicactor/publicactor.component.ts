import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActorEntity } from '../../model/entity/ActorEntity';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { PublicVideoService } from '../../service/PublicVideoService';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';
import { PaginationDto } from '../../../shared/model/dto/PaginationDto';
import { VideoCaptureEntity } from '../../model/entity/VideoCaptureEntity';

interface ActorCaptureGallery {
  Video: VideoCardDto;
  Captures: VideoCaptureEntity[];
}

@Component({
  selector: 'app-publicactor',
  templateUrl: './publicactor.component.html'
})
export class PublicactorComponent implements OnInit {
  actorCod = '';
  sortMode = 'recent';
  actor: ActorEntity = new ActorEntity();
  videos: VideoCardDto[] = [];
  page = 1;
  limit = 24;
  totalRows = 0;
  pagination = new PaginationDto({ Limit: this.limit, ItemLabel: 'videos' });
  captureGalleries: ActorCaptureGallery[] = [];
  captureGalleryVisible = false;
  captureGalleryLoading = false;
  captureGalleryMessage = '';

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, public publicPreferenceService: PublicPreferenceService) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextActorCod = params.get('actorCod') || '';
      if (!nextActorCod) {
        return;
      }
      this.actorCod = nextActorCod;
      this.captureGalleries = [];
      this.captureGalleryVisible = false;
      this.captureGalleryMessage = '';
      await this.loadVideos('recent', 1);
    });
  }

  async loadVideos(sort: string, page: number = 1): Promise<void> {
    this.sortMode = sort;
    this.page = page < 1 ? 1 : page;
    const rpt = await this.publicVideoService.findByActor(this.actorCod, sort, this.page, this.limit);
    this.videos = rpt.Data?.Data || [];
    this.totalRows = rpt.Data?.TotalRows || 0;
    this.page = rpt.Data?.Page || this.page;
    this.limit = rpt.Data?.Limit || this.limit;
    this.pagination = new PaginationDto({ Page: this.page, Limit: this.limit, TotalRows: this.totalRows, ItemLabel: 'videos' });
    this.actor = rpt.DataAdditional?.Actor || new ActorEntity();
  }

  actorImage(): string {
    return this.actor.ImageUrl || 'assets/default-actor.svg';
  }

  actorDescription(): string {
    return this.actor.Description || 'Descripcion no registrada.';
  }

  onActorImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.src = 'assets/default-actor.svg';
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }

  async showCaptureGalleries(): Promise<void> {
    this.captureGalleryVisible = true;
    if (this.captureGalleries.length > 0) {
      setTimeout(() => document.getElementById('actorCaptureGallery')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
      return;
    }
    this.captureGalleryLoading = true;
    this.captureGalleryMessage = '';
    try {
      const rpt = await this.publicVideoService.findActorCaptureGalleries(this.actorCod);
      if (rpt.ErrorStatus) {
        this.captureGalleryMessage = rpt.Message;
        return;
      }
      this.captureGalleries = rpt.Data || [];
      if (this.captureGalleries.length === 0) {
        this.captureGalleryMessage = 'No hay capturas disponibles para los videos de este actor.';
      }
      setTimeout(() => document.getElementById('actorCaptureGallery')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
    } catch {
      this.captureGalleryMessage = 'No se pudieron cargar las capturas del actor.';
    } finally {
      this.captureGalleryLoading = false;
    }
  }
}
