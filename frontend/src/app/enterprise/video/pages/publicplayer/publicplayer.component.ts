import { Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { PublicAuthService } from '../../service/PublicAuthService';
import { PublicSubscriberService } from '../../service/PublicSubscriberService';
import { PublicVideoService } from '../../service/PublicVideoService';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';
import { VideoDetailDto } from '../../model/dto/VideoDetailDto';
import { VideoCardDto } from '../../model/dto/VideoCardDto';

@Component({
  selector: 'app-publicplayer',
  templateUrl: './publicplayer.component.html'
})
export class PublicplayerComponent implements OnInit, OnDestroy {
  @ViewChild('captureSlider') captureSlider?: ElementRef<HTMLDivElement>;
  @ViewChild('videoPlayer') videoPlayer?: ElementRef<HTMLVideoElement>;

  videoCod = '';
  detail: VideoDetailDto = new VideoDetailDto();
  related: VideoCardDto[] = [];
  embedHtml: SafeHtml | null = null;
  errorMessage = '';
  playerErrorMessage = '';
  detailLoaded = false;
  viewRegistered = false;
  showCaptureModal = false;
  selectedCaptureIndex = 0;
  viewerState: any = {};
  interactionMessage = '';
  suggestionComment = '';
  suggestionMessage = '';
  navigationMessage = '';
  private viewLogId: number | null = null;
  private pendingWatchSeconds = 0;
  private lastWatchPosition: number | null = null;
  private lastWatchFlushAt = 0;
  private capturesGenerationRunning = false;
  private captureRefreshTimer: ReturnType<typeof setInterval> | null = null;
  private suggestionMessageTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(private route: ActivatedRoute, private router: Router, private publicVideoService: PublicVideoService, private publicSubscriberService: PublicSubscriberService, public publicAuthService: PublicAuthService, public publicPreferenceService: PublicPreferenceService, private sanitizer: DomSanitizer) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextVideoCod = params.get('videoCod') || '';
      if (!nextVideoCod) {
        return;
      }
      await this.flushWatchProgress();
      this.videoCod = nextVideoCod;
      this.resetState();
      await this.load();
    });
  }

  ngOnDestroy(): void {
    this.flushWatchProgress();
    this.stopCaptureRefresh();
    this.clearSuggestionMessageTimer();
  }

  @HostListener('window:beforeunload')
  onBeforeUnload(): void {
    this.flushWatchProgress();
  }

  async load(): Promise<void> {
    const detailRpt = await this.publicVideoService.findDetail(this.videoCod);
    if (detailRpt.ErrorStatus) {
      this.errorMessage = detailRpt.Message;
      return;
    }
    this.detail = detailRpt.Data;
    this.detailLoaded = true;
    this.playerErrorMessage = '';
    if (this.detail.Video.SourceType === 'EMBED' && this.detail.Video.SourceValue) {
      this.embedHtml = this.sanitizer.bypassSecurityTrustHtml(this.detail.Video.SourceValue);
    }
    const relatedRpt = await this.publicVideoService.findRelated(this.videoCod);
    this.related = relatedRpt.Data || [];
    await this.registerViewOnce();
    await this.loadViewerState();
    void this.ensureAutomaticCapturesInBackground(this.videoCod);
  }

  resetState(): void {
    this.detail = new VideoDetailDto();
    this.related = [];
    this.embedHtml = null;
    this.errorMessage = '';
    this.playerErrorMessage = '';
    this.detailLoaded = false;
    this.viewRegistered = false;
    this.viewLogId = null;
    this.pendingWatchSeconds = 0;
    this.lastWatchPosition = null;
    this.lastWatchFlushAt = 0;
    this.viewerState = {};
    this.interactionMessage = '';
    this.suggestionComment = '';
    this.suggestionMessage = '';
    this.navigationMessage = '';
    this.capturesGenerationRunning = false;
    this.stopCaptureRefresh();
    this.clearSuggestionMessageTimer();
  }

  async registerViewOnce(): Promise<void> {
    if (this.viewRegistered) {
      return;
    }
    this.viewRegistered = true;
    const rpt = await this.publicVideoService.registerView(this.videoCod);
    if (!rpt.ErrorStatus) {
      this.viewLogId = rpt.Data?.ViewLogId || null;
    }
  }

  canUseHtmlVideo(): boolean {
    return this.detail.Video.SourceType === 'URL' || this.detail.Video.SourceType === 'PATH';
  }

  videoSource(): string {
    if (this.detail.Video.SourceType === 'PATH') {
      return `${AppSetting.API}/api/v1/public/videos/${this.detail.Video.VideoCod}/stream`;
    }
    return this.detail.Video.SourceValue;
  }

  onPlayerError(event: Event): void {
    const video = event.target as HTMLVideoElement;
    const code = video.error?.code;
    if (code === MediaError.MEDIA_ERR_SRC_NOT_SUPPORTED) {
      this.playerErrorMessage = 'El navegador no pudo reproducir este archivo. Revisa el codec del MP4 o la respuesta del endpoint de streaming.';
      return;
    }
    this.playerErrorMessage = `No se pudo cargar el video desde el servidor. Codigo de error: ${code || 'desconocido'}.`;
  }

  onPlayerPlay(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.lastWatchPosition = video.currentTime;
  }

  onPlayerTimeUpdate(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addWatchDelta(video);
    if (this.pendingWatchSeconds >= 10 || Date.now() - this.lastWatchFlushAt >= 10000) {
      this.flushWatchProgress();
    }
  }

  onPlayerPause(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addWatchDelta(video);
    this.flushWatchProgress();
  }

  onPlayerSeeked(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.lastWatchPosition = video.currentTime;
    this.flushWatchProgress();
  }

  onPlayerEnded(event: Event): void {
    const video = event.target as HTMLVideoElement;
    this.addWatchDelta(video);
    this.flushWatchProgress(true);
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }

  scrollCaptures(direction: 'left' | 'right'): void {
    const slider = this.captureSlider?.nativeElement;
    if (!slider) {
      return;
    }
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

  selectedCapture(): any {
    return this.detail.Captures[this.selectedCaptureIndex] || null;
  }

  previousCapture(): void {
    if (!this.detail.Captures.length) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex === 0 ? this.detail.Captures.length - 1 : this.selectedCaptureIndex - 1;
  }

  nextCapture(): void {
    if (!this.detail.Captures.length) return;
    this.selectedCaptureIndex = this.selectedCaptureIndex >= this.detail.Captures.length - 1 ? 0 : this.selectedCaptureIndex + 1;
  }

  async loadViewerState(): Promise<void> {
    if (!this.publicAuthService.isLogged()) return;
    const rpt = await this.publicSubscriberService.state(this.videoCod);
    if (!rpt.ErrorStatus) this.viewerState = rpt.Data || {};
  }

  async react(reactionType: 'LIKE' | 'DISLIKE'): Promise<void> {
    const enabled = this.viewerState.ReactionType !== reactionType;
    const rpt = await this.publicSubscriberService.reaction(this.videoCod, reactionType, enabled);
    this.handleInteractionResponse(rpt);
  }

  async rate(value: number): Promise<void> {
    const rpt = await this.publicSubscriberService.rating(this.videoCod, value);
    this.handleInteractionResponse(rpt);
  }

  async toggleWatchLater(): Promise<void> {
    const rpt = await this.publicSubscriberService.watchLater(this.videoCod, !this.viewerState.WatchLater);
    this.handleInteractionResponse(rpt);
  }

  async suggestCurrentCapture(): Promise<void> {
    if (!this.publicAuthService.isLogged()) {
      this.showSuggestionMessage('Ingresa para sugerir capturas.');
      return;
    }
    if (this.detail.Video.SourceType !== 'PATH') {
      this.showSuggestionMessage('La sugerencia de captura solo esta disponible para videos del servidor.');
      return;
    }
    const second = this.videoPlayer?.nativeElement?.currentTime || 0;
    const rpt = await this.publicSubscriberService.suggestCapture(this.videoCod, second, this.suggestionComment);
    if (rpt.ErrorStatus) {
      this.showSuggestionMessage(rpt.Message);
      return;
    }
    this.suggestionComment = '';
    this.showSuggestionMessage('Captura enviada para revision del administrador.');
  }

  async goRandomVideo(): Promise<void> {
    const rpt = await this.publicVideoService.findRandom(this.videoCod);
    if (rpt.ErrorStatus || !rpt.Data?.VideoCod) {
      this.navigationMessage = rpt.Message || 'No se encontro otro video disponible.';
      return;
    }
    await this.navigateToVideo(rpt.Data.VideoCod);
  }

  async goRecommendedVideo(): Promise<void> {
    const candidates = (this.related || []).filter(video => video.VideoCod && video.VideoCod !== this.videoCod);
    if (candidates.length > 0) {
      const selected = candidates[Math.floor(Math.random() * candidates.length)];
      await this.navigateToVideo(selected.VideoCod);
      return;
    }
    await this.goRandomVideo();
  }

  private async ensureAutomaticCapturesInBackground(videoCod: string): Promise<void> {
    if (this.capturesGenerationRunning || this.detail.Video.SourceType !== 'PATH') {
      return;
    }
    const hasAutomaticCaptures = (this.detail.Captures || []).some(capture => capture.CaptureSource === 'AUTO');
    if (hasAutomaticCaptures) {
      return;
    }
    this.capturesGenerationRunning = true;
    try {
      await this.publicVideoService.ensureCaptures(videoCod);
      this.startCaptureRefresh(videoCod);
    } catch {
    } finally {
      if (this.videoCod === videoCod) {
        this.capturesGenerationRunning = false;
      }
    }
  }

  private startCaptureRefresh(videoCod: string): void {
    this.stopCaptureRefresh();
    this.captureRefreshTimer = setInterval(() => {
      void this.refreshCapturesIfReady(videoCod);
    }, 15000);
  }

  private stopCaptureRefresh(): void {
    if (!this.captureRefreshTimer) {
      return;
    }
    clearInterval(this.captureRefreshTimer);
    this.captureRefreshTimer = null;
  }

  private async refreshCapturesIfReady(videoCod: string): Promise<void> {
    if (this.videoCod !== videoCod) {
      this.stopCaptureRefresh();
      return;
    }
    try {
      const detailRpt = await this.publicVideoService.findDetail(videoCod);
      const captures = detailRpt.Data?.Captures || [];
      const hasAutomaticCaptures = captures.some((capture: any) => capture.CaptureSource === 'AUTO');
      if (!detailRpt.ErrorStatus && hasAutomaticCaptures && this.videoCod === videoCod) {
        this.detail.Captures = captures;
        this.detail.Video.Duration = detailRpt.Data?.Video?.Duration || this.detail.Video.Duration;
        this.stopCaptureRefresh();
      }
    } catch {
    }
  }

  private handleInteractionResponse(rpt: any): void {
    if (rpt.ErrorStatus) {
      this.interactionMessage = rpt.Message;
      return;
    }
    this.viewerState = rpt.Data || {};
    this.interactionMessage = 'Preferencia guardada.';
  }

  private async navigateToVideo(videoCod: string): Promise<void> {
    await this.flushWatchProgress();
    await this.router.navigate(['/video', videoCod]);
  }

  private showSuggestionMessage(message: string): void {
    this.clearSuggestionMessageTimer();
    this.suggestionMessage = message;
    this.suggestionMessageTimer = setTimeout(() => {
      this.suggestionMessage = '';
      this.suggestionMessageTimer = null;
    }, 3500);
  }

  private clearSuggestionMessageTimer(): void {
    if (!this.suggestionMessageTimer) {
      return;
    }
    clearTimeout(this.suggestionMessageTimer);
    this.suggestionMessageTimer = null;
  }

  private addWatchDelta(video: HTMLVideoElement): void {
    if (video.paused || video.seeking) {
      this.lastWatchPosition = video.currentTime;
      return;
    }
    if (this.lastWatchPosition !== null) {
      const delta = video.currentTime - this.lastWatchPosition;
      if (delta > 0 && delta <= 2.5) {
        this.pendingWatchSeconds += delta;
      }
    }
    this.lastWatchPosition = video.currentTime;
  }

  private async flushWatchProgress(completed: boolean = false): Promise<void> {
    if (!this.videoCod || (!this.viewLogId && this.pendingWatchSeconds <= 0 && !completed)) {
      return;
    }
    const videoCod = this.videoCod;
    const viewLogId = this.viewLogId;
    const video = this.videoPlayer?.nativeElement;
    const playedSeconds = Number(this.pendingWatchSeconds.toFixed(3));
    this.pendingWatchSeconds = 0;
    this.lastWatchFlushAt = Date.now();
    const rpt = await this.publicVideoService.registerWatchProgress(videoCod, {
      ViewLogId: viewLogId,
      VideoCod: videoCod,
      PlayerContext: 'PUBLIC_PLAYER',
      PlayedSeconds: playedSeconds,
      CurrentSecond: video ? Number(video.currentTime.toFixed(3)) : null,
      DurationSeconds: video && Number.isFinite(video.duration) ? Number(video.duration.toFixed(3)) : null,
      Completed: completed
    });
    if (!rpt.ErrorStatus && this.videoCod === videoCod && this.viewLogId === viewLogId) {
      this.viewLogId = rpt.Data?.ViewLogId || this.viewLogId;
    }
  }
}
