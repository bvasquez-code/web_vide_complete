import { Component, OnInit } from '@angular/core';
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
  videoCod = '';
  detail: VideoDetailDto = new VideoDetailDto();
  related: VideoCardDto[] = [];
  embedHtml: SafeHtml | null = null;
  errorMessage = '';
  playerErrorMessage = '';
  viewRegistered = false;

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, private sanitizer: DomSanitizer) {}

  async ngOnInit(): Promise<void> {
    this.videoCod = this.route.snapshot.paramMap.get('videoCod') || '';
    await this.load();
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
}
