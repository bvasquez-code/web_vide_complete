import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActorEntity } from '../../model/entity/ActorEntity';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { VideoCaptureEntity } from '../../model/entity/VideoCaptureEntity';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';
import { PublicVideoService } from '../../service/PublicVideoService';

interface ActorCaptureGallery {
  Video: VideoCardDto;
  Captures: VideoCaptureEntity[];
}

@Component({
  selector: 'app-publicactorphotos',
  templateUrl: './publicactorphotos.component.html'
})
export class PublicactorphotosComponent implements OnInit {
  actorCod = '';
  actor: ActorEntity = new ActorEntity();
  captureGalleries: ActorCaptureGallery[] = [];
  loading = false;
  errorMessage = '';
  showCaptureModal = false;
  selectedGroupIndex = 0;
  selectedCaptureIndex = 0;

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, public publicPreferenceService: PublicPreferenceService) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextActorCod = params.get('actorCod') || '';
      if (!nextActorCod) {
        return;
      }
      this.actorCod = nextActorCod;
      await this.load();
    });
  }

  async load(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';
    this.captureGalleries = [];
    try {
      const rpt = await this.publicVideoService.findActorCaptureGalleries(this.actorCod);
      if (rpt.ErrorStatus) {
        this.errorMessage = rpt.Message;
        return;
      }
      this.actor = rpt.DataAdditional?.Actor || new ActorEntity();
      this.captureGalleries = rpt.Data || [];
      if (this.captureGalleries.length === 0) {
        this.errorMessage = 'No hay capturas disponibles para los videos de este actor.';
      }
    } catch {
      this.errorMessage = 'No se pudieron cargar las fotos del actor.';
    } finally {
      this.loading = false;
    }
  }

  actorTitle(): string {
    return this.publicPreferenceService.showThumbnails() ? (this.actor.Name || 'Actor') : this.actor.ActorCod;
  }

  openCaptureModal(groupIndex: number, captureIndex: number): void {
    this.selectedGroupIndex = groupIndex;
    this.selectedCaptureIndex = captureIndex;
    this.showCaptureModal = true;
  }

  closeCaptureModal(): void {
    this.showCaptureModal = false;
  }

  selectedGroup(): ActorCaptureGallery | null {
    return this.captureGalleries[this.selectedGroupIndex] || null;
  }

  selectedCapture(): VideoCaptureEntity | null {
    return this.selectedGroup()?.Captures[this.selectedCaptureIndex] || null;
  }

  previousCapture(): void {
    const group = this.selectedGroup();
    if (!group?.Captures.length) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex === 0 ? group.Captures.length - 1 : this.selectedCaptureIndex - 1;
  }

  nextCapture(): void {
    const group = this.selectedGroup();
    if (!group?.Captures.length) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex >= group.Captures.length - 1 ? 0 : this.selectedCaptureIndex + 1;
  }
}
