import { Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { VideoRegisterDto } from '../../../video/model/dto/VideoRegisterDto';
import { VideoCategoryEntity } from '../../../video/model/entity/VideoCategoryEntity';
import { ActorEntity } from '../../../video/model/entity/ActorEntity';
import { TagEntity } from '../../../video/model/entity/TagEntity';
import { VideoCaptureEntity } from '../../../video/model/entity/VideoCaptureEntity';

@Component({
  selector: 'app-createvideo',
  templateUrl: './createvideo.component.html'
})
export class CreatevideoComponent implements OnInit, OnDestroy {
  @ViewChild('previewVideo') previewVideo?: ElementRef<HTMLVideoElement>;
  @ViewChild('adminCaptureSlider') adminCaptureSlider?: ElementRef<HTMLDivElement>;

  dto: VideoRegisterDto = new VideoRegisterDto();
  categories: VideoCategoryEntity[] = [];
  actors: ActorEntity[] = [];
  tags: TagEntity[] = [];
  captures: VideoCaptureEntity[] = [];
  sourceTypes: string[] = ['EMBED', 'URL', 'PATH'];
  errorMessage = '';
  saveMessage = '';
  saveMessageType: 'success' | 'error' = 'success';
  previewSource = '';
  previewError = '';
  previewEmbedHtml: SafeHtml | null = null;
  analyzeLoading = false;
  analyzeMessage = '';
  captureLoading = false;
  captureMessage = '';
  generateCapturesLoading = false;
  generateCapturesMessage = '';
  thumbnailOptions: string[] = [];
  showThumbnailModal = false;
  showCaptureModal = false;
  selectedCaptureIndex = 0;
  renameFileName = '';
  renameMessage = '';
  renameLoading = false;
  quickModalType: 'category' | 'actor' | 'tag' | '' = '';
  quickName = '';
  quickDescription = '';
  quickError = '';
  actorQuery = '';
  tagQuery = '';
  private previewViewLogId: number | null = null;
  private previewPendingWatchSeconds = 0;
  private previewLastWatchPosition: number | null = null;
  private previewLastWatchFlushAt = 0;

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
      this.captures = this.sortCaptures(rpt.Data.Captures || []);
      this.renameFileName = this.fileNameFromPath(this.dto.Video.SourceValue);
      this.loadPreview();
    }
  }

  ngOnDestroy(): void {
    this.flushPreviewWatchProgress();
  }

  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    this.flushPreviewWatchProgress();
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

  selectedActors(): ActorEntity[] {
    return this.actors.filter(item => this.dto.ActorCodList.includes(item.ActorCod));
  }

  selectedTags(): TagEntity[] {
    return this.tags.filter(item => this.dto.TagCodList.includes(item.TagCod));
  }

  filteredActors(): ActorEntity[] {
    const query = this.actorQuery.trim().toLowerCase();
    if (!query) return [];
    return this.actors
      .filter(item => item.Name.toLowerCase().includes(query) && !this.dto.ActorCodList.includes(item.ActorCod))
      .slice(0, 8);
  }

  filteredTags(): TagEntity[] {
    const query = this.tagQuery.trim().toLowerCase();
    if (!query) return [];
    return this.tags
      .filter(item => item.Name.toLowerCase().includes(query) && !this.dto.TagCodList.includes(item.TagCod))
      .slice(0, 8);
  }

  addActor(actor: ActorEntity): void {
    if (!this.dto.ActorCodList.includes(actor.ActorCod)) {
      this.dto.ActorCodList.push(actor.ActorCod);
    }
    this.actorQuery = '';
  }

  addTag(tag: TagEntity): void {
    if (!this.dto.TagCodList.includes(tag.TagCod)) {
      this.dto.TagCodList.push(tag.TagCod);
    }
    this.tagQuery = '';
  }

  removeActor(actorCod: string): void {
    this.dto.ActorCodList = this.dto.ActorCodList.filter(item => item !== actorCod);
  }

  removeTag(tagCod: string): void {
    this.dto.TagCodList = this.dto.TagCodList.filter(item => item !== tagCod);
  }

  async addActorFromQuery(): Promise<void> {
    const existing = this.actors.find(item => item.Name.toLowerCase() === this.actorQuery.trim().toLowerCase());
    if (existing) {
      this.addActor(existing);
      return;
    }
    this.openQuickModal('actor', this.actorQuery);
  }

  async addTagFromQuery(): Promise<void> {
    const existing = this.tags.find(item => item.Name.toLowerCase() === this.tagQuery.trim().toLowerCase());
    if (existing) {
      this.addTag(existing);
      return;
    }
    this.openQuickModal('tag', this.tagQuery);
  }

  openQuickModal(type: 'category' | 'actor' | 'tag', initialName: string = ''): void {
    this.quickModalType = type;
    this.quickName = initialName.trim();
    this.quickDescription = '';
    this.quickError = '';
  }

  closeQuickModal(): void {
    this.quickModalType = '';
    this.quickName = '';
    this.quickDescription = '';
    this.quickError = '';
  }

  quickTitle(): string {
    if (this.quickModalType === 'category') return 'Nueva categoria';
    if (this.quickModalType === 'actor') return 'Nuevo actor';
    return 'Nuevo tag';
  }

  async saveQuickEntity(): Promise<void> {
    this.quickError = '';
    if (!this.quickName.trim()) {
      this.quickError = 'El nombre es obligatorio.';
      return;
    }
    if (this.quickModalType === 'category') {
      const entity = new VideoCategoryEntity();
      entity.Name = this.quickName.trim();
      entity.Description = this.quickDescription.trim();
      entity.Status = 'A';
      const rpt = await this.adminVideoService.saveCategory(entity);
      if (rpt.ErrorStatus) {
        this.quickError = rpt.Message;
        return;
      }
      this.categories.push(rpt.Data);
      this.toggle(this.dto.CategoryCodList, rpt.Data.CategoryCod, true);
    }
    if (this.quickModalType === 'actor') {
      const entity = new ActorEntity();
      entity.Name = this.quickName.trim();
      entity.Description = this.quickDescription.trim();
      entity.Status = 'A';
      const rpt = await this.adminVideoService.saveActor(entity);
      if (rpt.ErrorStatus) {
        this.quickError = rpt.Message;
        return;
      }
      this.actors.push(rpt.Data);
      this.addActor(rpt.Data);
    }
    if (this.quickModalType === 'tag') {
      const entity = new TagEntity();
      entity.Name = this.quickName.trim();
      entity.Status = 'A';
      const rpt = await this.adminVideoService.saveTag(entity);
      if (rpt.ErrorStatus) {
        this.quickError = rpt.Message;
        return;
      }
      this.tags.push(rpt.Data);
      this.addTag(rpt.Data);
    }
    this.closeQuickModal();
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
    this.flushPreviewWatchProgress();
    this.previewError = '';
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.resetPreviewWatchTracking();
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
    this.flushPreviewWatchProgress();
    this.previewError = '';
    this.previewSource = '';
    this.previewEmbedHtml = null;
    this.resetPreviewWatchTracking();
    this.analyzeMessage = '';
    this.captureMessage = '';
  }

  canRenamePathFile(): boolean {
    return !!this.dto.Video.VideoCod && this.dto.Video.SourceType === 'PATH' && !!this.dto.Video.SourceValue;
  }

  async confirmRenameFile(): Promise<void> {
    if (!this.canRenamePathFile()) {
      this.renameMessage = 'Primero guarde un video con origen PATH.';
      return;
    }
    const newName = this.renameFileName.trim();
    if (!newName) {
      this.renameMessage = 'Ingrese el nuevo nombre del archivo.';
      return;
    }
    const currentName = this.fileNameFromPath(this.dto.Video.SourceValue);
    const ok = window.confirm(`Confirme que desea renombrar el archivo:\n\n${currentName}\n\na:\n\n${newName}`);
    if (!ok) {
      return;
    }
    this.renameLoading = true;
    this.renameMessage = '';
    try {
      const rpt = await this.adminVideoService.renameVideoFile(this.dto.Video.VideoCod, newName);
      if (rpt.ErrorStatus) {
        this.renameMessage = rpt.Message || 'No se pudo renombrar el archivo.';
        return;
      }
      this.dto.Video = rpt.Data;
      this.renameFileName = this.fileNameFromPath(this.dto.Video.SourceValue);
      this.clearPreview();
      this.renameMessage = 'Archivo renombrado correctamente.';
      this.showSaveMessage('Archivo renombrado correctamente.', 'success');
    } catch {
      this.renameMessage = 'No se pudo renombrar el archivo.';
    } finally {
      this.renameLoading = false;
    }
  }

  async generateVideoCaptures(): Promise<void> {
    this.generateCapturesMessage = '';
    if (!this.dto.Video.VideoCod) {
      this.generateCapturesMessage = 'Primero guarde el video para generar sus capturas.';
      return;
    }
    this.generateCapturesLoading = true;
    try {
      const rpt = await this.adminVideoService.generateCaptures(this.dto.Video.VideoCod);
      if (rpt.ErrorStatus) {
        this.generateCapturesMessage = rpt.Message;
        return;
      }
      this.dto.Video.Duration = rpt.Data?.Duration || this.dto.Video.Duration;
      this.captures = this.sortCaptures(rpt.Data?.Captures || this.captures);
      this.generateCapturesMessage = `Capturas generadas correctamente: ${rpt.Data?.CaptureCount || 0}.`;
    } catch {
      this.generateCapturesMessage = 'No se pudieron generar las capturas del video.';
    } finally {
      this.generateCapturesLoading = false;
    }
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

  onPreviewPlay(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.previewLastWatchPosition = video.currentTime;
  }

  onPreviewTimeUpdate(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    if (this.previewPendingWatchSeconds >= 10 || Date.now() - this.previewLastWatchFlushAt >= 10000) {
      this.flushPreviewWatchProgress();
    }
  }

  onPreviewPause(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    this.flushPreviewWatchProgress();
  }

  onPreviewSeeked(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.previewLastWatchPosition = video.currentTime;
    this.flushPreviewWatchProgress();
  }

  onPreviewEnded(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addPreviewWatchDelta(video);
    this.flushPreviewWatchProgress(true);
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

  async captureCurrentFrame(): Promise<void> {
    this.captureMessage = '';
    if (!this.dto.Video.VideoCod) {
      this.captureMessage = 'Primero guarde el video para poder asociar la miniatura al codigo del video.';
      return;
    }
    const video = this.previewVideo?.nativeElement;
    if (!video || !this.previewSource) {
      this.captureMessage = 'Primero cargue la previsualizacion del video.';
      return;
    }
    if (video.readyState < 2) {
      this.captureMessage = 'El video todavia no esta listo para capturar.';
      return;
    }
    this.captureLoading = true;
    try {
      const blob = await this.captureFrameFromSource(this.previewSource, video.currentTime);
      const rpt = await this.adminVideoService.uploadThumbnail(this.dto.Video.VideoCod, blob);
      if (rpt.ErrorStatus) {
        this.captureMessage = rpt.Message;
        return;
      }
      this.dto.Video.ThumbnailUrl = rpt.Data;
      this.captureMessage = 'Miniatura guardada correctamente.';
    } catch {
      this.captureMessage = 'No se pudo capturar la miniatura. Si es una URL externa, el servidor puede estar bloqueando CORS.';
    } finally {
      this.captureLoading = false;
    }
  }

  async saveCurrentFrameAsCapture(): Promise<void> {
    this.captureMessage = '';
    if (!this.dto.Video.VideoCod) {
      this.captureMessage = 'Primero guarde el video para poder asociar la captura al codigo del video.';
      return;
    }
    const video = this.previewVideo?.nativeElement;
    if (!video || !this.previewSource) {
      this.captureMessage = 'Primero cargue la previsualizacion del video.';
      return;
    }
    if (video.readyState < 2) {
      this.captureMessage = 'El video todavia no esta listo para capturar.';
      return;
    }
    this.captureLoading = true;
    try {
      const existing = new Set(this.captures.map(capture => capture.CaptureId));
      const captureSecond = Number(video.currentTime.toFixed(3));
      const rpt = await this.adminVideoService.captureAtSecond(this.dto.Video.VideoCod, captureSecond);
      if (rpt.ErrorStatus) {
        this.captureMessage = rpt.Message;
        return;
      }
      if (!existing.has(rpt.Data.CaptureId)) {
        this.captures.push(rpt.Data);
        this.captures = this.sortCaptures(this.captures);
        this.captureMessage = `Captura guardada correctamente en ${this.formatDuration(captureSecond)}.`;
      } else {
        this.captureMessage = 'La captura ya existia, no se guardo duplicada.';
      }
    } catch {
      this.captureMessage = 'No se pudo guardar la captura. Verifique que el video sea de tipo PATH y que FFmpeg pueda leer el archivo.';
    } finally {
      this.captureLoading = false;
    }
  }

  selectThumbnail(image: string): void {
    this.dto.Video.ThumbnailUrl = image;
    this.showThumbnailModal = false;
  }

  closeThumbnailModal(): void {
    this.showThumbnailModal = false;
  }

  scrollAdminCaptures(direction: 'left' | 'right'): void {
    const slider = this.adminCaptureSlider?.nativeElement;
    if (!slider) return;
    const distance = Math.max(260, slider.clientWidth * 0.8);
    slider.scrollBy({ left: direction === 'left' ? -distance : distance, behavior: 'smooth' });
  }

  openCaptureModal(index: number): void {
    this.selectedCaptureIndex = index;
    this.showCaptureModal = true;
  }

  closeCaptureModal(): void {
    this.showCaptureModal = false;
  }

  selectedCapture(): VideoCaptureEntity | null {
    return this.captures[this.selectedCaptureIndex] || null;
  }

  previousCapture(): void {
    if (this.captures.length === 0) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex === 0 ? this.captures.length - 1 : this.selectedCaptureIndex - 1;
  }

  nextCapture(): void {
    if (this.captures.length === 0) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex >= this.captures.length - 1 ? 0 : this.selectedCaptureIndex + 1;
  }

  async useSelectedCaptureAsThumbnail(): Promise<void> {
    const capture = this.selectedCapture();
    if (!capture || !this.dto.Video.VideoCod) return;
    this.captureMessage = '';
    this.captureLoading = true;
    try {
      const rpt = await this.adminVideoService.useCaptureAsThumbnail(this.dto.Video.VideoCod, capture.CaptureId);
      if (rpt.ErrorStatus) {
        this.captureMessage = rpt.Message;
        return;
      }
      this.dto.Video.ThumbnailUrl = capture.ImageUrl;
      this.captureMessage = 'Miniatura actualizada desde la captura seleccionada.';
    } catch {
      this.captureMessage = 'No se pudo actualizar la miniatura desde esta captura.';
    } finally {
      this.captureLoading = false;
    }
  }

  async deleteSelectedCapture(): Promise<void> {
    const capture = this.selectedCapture();
    if (!capture || !this.dto.Video.VideoCod) return;
    const confirmed = window.confirm('Se eliminara esta captura del video. Desea continuar?');
    if (!confirmed) return;
    this.captureMessage = '';
    this.captureLoading = true;
    try {
      const rpt = await this.adminVideoService.deleteCapture(this.dto.Video.VideoCod, capture.CaptureId);
      if (rpt.ErrorStatus) {
        this.captureMessage = rpt.Message;
        return;
      }
      if (this.dto.Video.ThumbnailUrl === capture.ImageUrl) {
        this.dto.Video.ThumbnailUrl = '';
      }
      this.captures = this.captures.filter(item => item.CaptureId !== capture.CaptureId);
      if (this.captures.length === 0) {
        this.closeCaptureModal();
      } else if (this.selectedCaptureIndex >= this.captures.length) {
        this.selectedCaptureIndex = this.captures.length - 1;
      }
      this.captureMessage = 'Captura eliminada correctamente.';
    } catch {
      this.captureMessage = 'No se pudo eliminar la captura seleccionada.';
    } finally {
      this.captureLoading = false;
    }
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

  private captureFrameFromSource(source: string, currentTime: number): Promise<Blob> {
    return new Promise((resolve, reject) => {
      const video = document.createElement('video');
      video.preload = 'auto';
      video.crossOrigin = 'anonymous';
      video.muted = true;
      video.src = source;
      video.onloadedmetadata = () => {
        try {
          video.currentTime = Math.max(0, Math.min(currentTime, Number.isFinite(video.duration) ? video.duration : currentTime));
        } catch {
          reject();
        }
      };
      video.onseeked = () => this.captureCurrentFrameBlob(video).then(resolve).catch(reject);
      video.onerror = () => reject();
      video.load();
    });
  }

  private captureCurrentFrameBlob(video: HTMLVideoElement): Promise<Blob> {
    return new Promise((resolve, reject) => {
      try {
        const canvas = document.createElement('canvas');
        canvas.width = 960;
        canvas.height = Math.round(960 * (video.videoHeight || 360) / (video.videoWidth || 640));
        const context = canvas.getContext('2d');
        if (!context) {
          reject();
          return;
        }
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        canvas.toBlob(blob => {
          if (!blob) {
            reject();
            return;
          }
          resolve(blob);
        }, 'image/jpeg', 0.85);
      } catch {
        reject();
      }
    });
  }

  private sortCaptures(captures: VideoCaptureEntity[]): VideoCaptureEntity[] {
    return [...captures].sort((a, b) => {
      const secondA = Number.isFinite(Number(a.CaptureSecond)) ? Number(a.CaptureSecond) : Number.MAX_SAFE_INTEGER;
      const secondB = Number.isFinite(Number(b.CaptureSecond)) ? Number(b.CaptureSecond) : Number.MAX_SAFE_INTEGER;
      if (secondA !== secondB) return secondA - secondB;
      return (a.DisplayOrder || 0) - (b.DisplayOrder || 0);
    });
  }

  private resetPreviewWatchTracking(): void {
    this.previewViewLogId = null;
    this.previewPendingWatchSeconds = 0;
    this.previewLastWatchPosition = null;
    this.previewLastWatchFlushAt = 0;
  }

  private fileNameFromPath(path: string): string {
    if (!path) {
      return '';
    }
    const normalized = path.replace(/\\/g, '/');
    return normalized.substring(normalized.lastIndexOf('/') + 1);
  }

  private addPreviewWatchDelta(video: HTMLVideoElement): void {
    if (video.paused || video.seeking) {
      this.previewLastWatchPosition = video.currentTime;
      return;
    }
    if (this.previewLastWatchPosition !== null) {
      const delta = video.currentTime - this.previewLastWatchPosition;
      if (delta > 0 && delta <= 2.5) {
        this.previewPendingWatchSeconds += delta;
      }
    }
    this.previewLastWatchPosition = video.currentTime;
  }

  private async flushPreviewWatchProgress(completed: boolean = false): Promise<void> {
    const videoCod = this.dto.Video.VideoCod;
    if (!videoCod || (!this.previewViewLogId && this.previewPendingWatchSeconds <= 0 && !completed)) {
      return;
    }
    const viewLogId = this.previewViewLogId;
    const video = this.previewVideo?.nativeElement;
    const playedSeconds = Number(this.previewPendingWatchSeconds.toFixed(3));
    this.previewPendingWatchSeconds = 0;
    this.previewLastWatchFlushAt = Date.now();
    const rpt = await this.adminVideoService.registerWatchProgress(videoCod, {
      ViewLogId: viewLogId,
      VideoCod: videoCod,
      PlayerContext: 'ADMIN_PREVIEW',
      PlayedSeconds: playedSeconds,
      CurrentSecond: video ? Number(video.currentTime.toFixed(3)) : null,
      DurationSeconds: video && Number.isFinite(video.duration) ? Number(video.duration.toFixed(3)) : null,
      Completed: completed
    });
    if (!rpt.ErrorStatus && this.dto.Video.VideoCod === videoCod && this.previewViewLogId === viewLogId) {
      this.previewViewLogId = rpt.Data?.ViewLogId || this.previewViewLogId;
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
    this.saveMessage = '';
    if (!this.validate()) return;
    const wasNewVideo = !this.dto.Video.VideoCod;
    const rpt = await this.adminVideoService.saveVideo(this.dto);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      this.showSaveMessage(rpt.Message || 'No se pudo guardar el video.', 'error');
      return;
    }
    this.dto.Video = rpt.Data;
    this.showSaveMessage('Video guardado correctamente.', 'success');
    if (wasNewVideo && this.dto.Video.VideoCod) {
      await this.router.navigate(['/admin/videos/edit', this.dto.Video.VideoCod], { replaceUrl: true });
      this.loadPreview();
    }
  }

  showSaveMessage(message: string, type: 'success' | 'error'): void {
    this.saveMessage = message;
    this.saveMessageType = type;
  }

  closeSaveMessage(): void {
    this.saveMessage = '';
  }
}
