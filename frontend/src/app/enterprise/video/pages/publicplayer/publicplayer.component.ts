import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ActivatedRoute } from '@angular/router';
import { AppSetting } from '../../../../config/AppSetting';
import { PublicVideoService } from '../../service/PublicVideoService';
import { VideoDetailDto } from '../../model/dto/VideoDetailDto';
import { VideoCardDto } from '../../model/dto/VideoCardDto';

@Component({
  selector: 'app-publicplayer',
  templateUrl: './publicplayer.component.html'
})
export class PublicplayerComponent implements OnInit {
  @ViewChild('captureSlider') captureSlider?: ElementRef<HTMLDivElement>;

  videoCod = '';
  detail: VideoDetailDto = new VideoDetailDto();
  related: VideoCardDto[] = [];
  embedHtml: SafeHtml | null = null;
  errorMessage = '';
  playerErrorMessage = '';
  viewRegistered = false;

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, private sanitizer: DomSanitizer) {}

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
  }

  resetState(): void {
    this.detail = new VideoDetailDto();
    this.related = [];
    this.embedHtml = null;
    this.errorMessage = '';
    this.playerErrorMessage = '';
    this.viewRegistered = false;
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
}
