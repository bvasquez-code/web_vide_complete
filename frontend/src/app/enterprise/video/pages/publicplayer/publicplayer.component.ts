import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { PublicAuthService } from '../../service/PublicAuthService';
import { PublicSubscriberService } from '../../service/PublicSubscriberService';
import { PublicVideoService } from '../../service/PublicVideoService';
import { VideoDetailDto } from '../../model/dto/VideoDetailDto';
import { VideoCardDto } from '../../model/dto/VideoCardDto';

@Component({
  selector: 'app-publicplayer',
  templateUrl: './publicplayer.component.html'
})
export class PublicplayerComponent implements OnInit {
  @ViewChild('captureSlider') captureSlider?: ElementRef<HTMLDivElement>;
  @ViewChild('videoPlayer') videoPlayer?: ElementRef<HTMLVideoElement>;

  videoCod = '';
  detail: VideoDetailDto = new VideoDetailDto();
  related: VideoCardDto[] = [];
  embedHtml: SafeHtml | null = null;
  errorMessage = '';
  playerErrorMessage = '';
  viewRegistered = false;
  showCaptureModal = false;
  selectedCaptureIndex = 0;
  viewerState: any = {};
  interactionMessage = '';
  suggestionComment = '';
  suggestionMessage = '';

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, private publicSubscriberService: PublicSubscriberService, public publicAuthService: PublicAuthService, private sanitizer: DomSanitizer) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextVideoCod = params.get('videoCod') || '';
      if (!nextVideoCod) {
        return;
      }
      this.videoCod = nextVideoCod;
      this.resetState();
      await this.load();
    });
  }

  async load(): Promise<void> {
    const detailRpt = await this.publicVideoService.findDetail(this.videoCod);
    if (detailRpt.ErrorStatus) {
      this.errorMessage = detailRpt.Message;
      return;
    }
    this.detail = detailRpt.Data;
    this.playerErrorMessage = '';
    if (this.detail.Video.SourceType === 'EMBED' && this.detail.Video.SourceValue) {
      this.embedHtml = this.sanitizer.bypassSecurityTrustHtml(this.detail.Video.SourceValue);
    }
    const relatedRpt = await this.publicVideoService.findRelated(this.videoCod);
    this.related = relatedRpt.Data || [];
    await this.registerViewOnce();
    await this.loadViewerState();
  }

  resetState(): void {
    this.detail = new VideoDetailDto();
    this.related = [];
    this.embedHtml = null;
    this.errorMessage = '';
    this.playerErrorMessage = '';
    this.viewRegistered = false;
    this.viewerState = {};
    this.interactionMessage = '';
    this.suggestionComment = '';
    this.suggestionMessage = '';
  }

  async registerViewOnce(): Promise<void> {
    if (this.viewRegistered) {
      return;
    }
    this.viewRegistered = true;
    await this.publicVideoService.registerView(this.videoCod);
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
    const second = this.videoPlayer?.nativeElement?.currentTime || 0;
    const rpt = await this.publicSubscriberService.suggestCapture(this.videoCod, second, this.suggestionComment);
    if (rpt.ErrorStatus) {
      this.suggestionMessage = rpt.Message;
      return;
    }
    this.suggestionComment = '';
    this.suggestionMessage = 'Captura enviada para revision del administrador.';
  }

  private handleInteractionResponse(rpt: any): void {
    if (rpt.ErrorStatus) {
      this.interactionMessage = rpt.Message;
      return;
    }
    this.viewerState = rpt.Data || {};
    this.interactionMessage = 'Preferencia guardada.';
  }
}
