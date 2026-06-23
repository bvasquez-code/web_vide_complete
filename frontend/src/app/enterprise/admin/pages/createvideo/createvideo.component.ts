import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { VideoRegisterDto } from '../../../video/model/dto/VideoRegisterDto';
import { VideoCategoryEntity } from '../../../video/model/entity/VideoCategoryEntity';
import { ActorEntity } from '../../../video/model/entity/ActorEntity';
import { TagEntity } from '../../../video/model/entity/TagEntity';

@Component({
  selector: 'app-createvideo',
  templateUrl: './createvideo.component.html'
})
export class CreatevideoComponent implements OnInit {
  dto: VideoRegisterDto = new VideoRegisterDto();
  categories: VideoCategoryEntity[] = [];
  actors: ActorEntity[] = [];
  tags: TagEntity[] = [];
  sourceTypes: string[] = ['EMBED', 'URL', 'PATH'];
  errorMessage = '';
  previewSource = '';
  previewError = '';
  previewEmbedHtml: SafeHtml | null = null;
  analyzeLoading = false;
  analyzeMessage = '';
  thumbnailOptions: string[] = [];
  showThumbnailModal = false;

  constructor(private adminVideoService: AdminVideoService, private route: ActivatedRoute, private router: Router, private sanitizer: DomSanitizer) {}

  async ngOnInit(): Promise<void> {
    const dataRpt = await this.adminVideoService.findDataForm();
    this.categories = dataRpt.Data.Categories || [];
    this.actors = dataRpt.Data.Actors || [];
    this.tags = dataRpt.Data.Tags || [];
    this.sourceTypes = dataRpt.Data.SourceTypes || this.sourceTypes;
    const videoCod = this.route.snapshot.paramMap.get('videoCod');
    if (videoCod) {
      const rpt = await this.adminVideoService.findVideo(videoCod);
      this.dto.Video = rpt.Data.Video;
      this.dto.CategoryCodList = (rpt.Data.Categories || []).map((x: any) => x.CategoryCod);
      this.dto.ActorCodList = (rpt.Data.Actors || []).map((x: any) => x.ActorCod);
      this.dto.TagCodList = (rpt.Data.Tags || []).map((x: any) => x.TagCod);
      this.loadPreview();
    }
  }

  isChecked(list: string[], value: string): boolean {
    return list.includes(value);
  }

  toggle(list: string[], value: string, checked: boolean): void {
    if (checked && !list.includes(value)) list.push(value);
    if (!checked) {
      const idx = list.indexOf(value);
      if (idx >= 0) list.splice(idx, 1);
    }
  }

  validate(): boolean {
    if (!this.dto.Video.Title || !this.dto.Video.SourceType || !this.dto.Video.SourceValue) {
      this.errorMessage = 'Titulo, tipo de origen y referencia son obligatorios.';
      return false;
    }
    if (this.dto.CategoryCodList.length === 0) {
      this.errorMessage = 'Debe seleccionar al menos una categoria.';
      return false;
    }
    return true;
  }

  loadPreview(): void {
    this.previewError = '';
    this.previewSource = '';
    this.previewEmbedHtml = null;
    if (!this.dto.Video.SourceType || !this.dto.Video.SourceValue) {
      this.previewError = 'Ingrese el origen y la referencia del video para previsualizar.';
      return;
    }
    if (this.dto.Video.SourceType === 'EMBED') {
      this.previewEmbedHtml = this.sanitizer.bypassSecurityTrustHtml(this.dto.Video.SourceValue);
      return;
    }
    if (this.dto.Video.SourceType === 'URL') {
      this.previewSource = this.dto.Video.SourceValue;
      return;
    }
    if (this.dto.Video.SourceType === 'PATH') {
      if (!this.dto.Video.VideoCod) {
        this.previewError = 'Para rutas del servidor, primero guarde el video. Luego podra previsualizarlo desde el endpoint de streaming.';
        return;
      }
      this.previewSource = `${AppSetting.API}/api/v1/public/videos/${this.dto.Video.VideoCod}/stream`;
    }
  }

  clearPreview(): void {
    this.previewError = '';
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.analyzeMessage = '';
  }

  onPreviewMetadata(event: Event): void {
    const video = event.target as HTMLVideoElement;
    if (Number.isFinite(video.duration) && video.duration > 0) {
      this.dto.Video.Duration = this.formatDuration(video.duration);
    }
  }

  onPreviewError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    const code = video.error?.code;
    this.previewError = `No se pudo cargar la previsualizacion del video. Codigo: ${code || 'desconocido'}.`;
  }

  async analyzeVideo(): Promise<void> {
    this.loadPreview();
    if (!this.previewSource) {
      return;
    }
    this.analyzeLoading = true;
    this.analyzeMessage = '';
    this.thumbnailOptions = [];
    try {
      const video = await this.createMetadataVideo(this.previewSource);
      if (Number.isFinite(video.duration) && video.duration > 0) {
        this.dto.Video.Duration = this.formatDuration(video.duration);
      }
      const duration = Number.isFinite(video.duration) && video.duration > 0 ? video.duration : 0;
      const targets = duration > 0
        ? [Math.max(0.5, duration * 0.1), duration * 0.3, duration * 0.5, duration * 0.75].map(x => Math.min(x, Math.max(0, duration - 0.2)))
        : [0];
      for (const second of targets) {
        const image = await this.captureThumbnailAt(video, second);
        if (image) {
          this.thumbnailOptions.push(image);
        }
      }
      if (this.thumbnailOptions.length > 0) {
        this.showThumbnailModal = true;
      } else {
        this.analyzeMessage = 'Se obtuvo la duracion, pero no se pudieron generar miniaturas.';
      }
    } catch {
      this.analyzeMessage = 'No se pudo analizar el video desde esta URL. Verifique que sea reproducible por el navegador y que permita lectura para miniaturas.';
    } finally {
      this.analyzeLoading = false;
    }
  }

  selectThumbnail(image: string): void {
    this.dto.Video.ThumbnailUrl = image;
    this.showThumbnailModal = false;
  }

  closeThumbnailModal(): void {
    this.showThumbnailModal = false;
  }

  private createMetadataVideo(source: string): Promise<HTMLVideoElement> {
    return new Promise((resolve, reject) => {
      const video = document.createElement('video');
      video.preload = 'metadata';
      video.crossOrigin = 'anonymous';
      video.muted = true;
      video.src = source;
      video.onloadedmetadata = () => resolve(video);
      video.onerror = () => reject();
      video.load();
    });
  }

  private captureThumbnailAt(video: HTMLVideoElement, second: number): Promise<string> {
    return new Promise(resolve => {
      const done = () => {
        try {
          resolve(this.captureThumbnail(video));
        } catch {
          resolve('');
        }
      };
      video.onseeked = done;
      video.onerror = () => resolve('');
      try {
        video.currentTime = second;
      } catch {
        done();
      }
    });
  }

  private captureThumbnail(video: HTMLVideoElement): string {
    try {
      const canvas = document.createElement('canvas');
      canvas.width = 640;
      canvas.height = Math.round(640 * (video.videoHeight || 360) / (video.videoWidth || 640));
      const context = canvas.getContext('2d');
      if (!context) return '';
      context.drawImage(video, 0, 0, canvas.width, canvas.height);
      return canvas.toDataURL('image/jpeg', 0.82);
    } catch {
      return '';
    }
  }

  private formatDuration(totalSeconds: number): string {
    const seconds = Math.floor(totalSeconds % 60).toString().padStart(2, '0');
    const minutesNumber = Math.floor(totalSeconds / 60);
    const minutes = (minutesNumber % 60).toString().padStart(2, '0');
    const hours = Math.floor(minutesNumber / 60);
    return hours > 0 ? `${hours}:${minutes}:${seconds}` : `${minutes}:${seconds}`;
  }

  async Save(): Promise<void> {
    this.errorMessage = '';
    if (!this.validate()) return;
    const rpt = await this.adminVideoService.saveVideo(this.dto);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    await this.router.navigate(['/admin/videos']);
  }
}
