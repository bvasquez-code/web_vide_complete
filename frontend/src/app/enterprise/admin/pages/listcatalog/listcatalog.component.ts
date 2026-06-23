import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { ActorEntity } from '../../../video/model/entity/ActorEntity';
import { TagEntity } from '../../../video/model/entity/TagEntity';
import { VideoCategoryEntity } from '../../../video/model/entity/VideoCategoryEntity';

@Component({
  selector: 'app-listcatalog',
  templateUrl: './listcatalog.component.html'
})
export class ListcatalogComponent implements OnInit {
  type = 'categories';
  Query = '';
  Status = '';
  rows: any[] = [];
  entity: any = {};
  errorMessage = '';
  saveMessage = '';
  saveMessageType: 'success' | 'error' = 'success';
  Page = 1;
  pageInput = 1;
  Limit = 20;
  TotalRows = 0;

  constructor(private route: ActivatedRoute, private router: Router, private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    this.route.url.subscribe(async segments => {
      this.type = segments[1]?.path || 'categories';
      const params = this.route.snapshot.queryParamMap;
      this.Query = params.get('Query') || '';
      this.Status = params.get('Status') || '';
      this.Page = Number(params.get('Page') || '1');
      this.Limit = Number(params.get('Limit') || '20');
      this.newEntity();
      await this.findAll(this.Page, false);
    });
  }

  title(): string {
    if (this.type === 'actors') return 'Actores';
    if (this.type === 'tags') return 'Tags';
    return 'Categorias';
  }

  newEntity(): void {
    this.saveMessage = '';
    if (this.type === 'actors') this.entity = new ActorEntity();
    else if (this.type === 'tags') this.entity = new TagEntity();
    else this.entity = new VideoCategoryEntity();
  }

  edit(row: any): void {
    this.saveMessage = '';
    this.entity = { ...row };
  }

  async findAll(page: number = this.Page, syncUrl: boolean = true): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    const rpt = this.type === 'actors'
      ? await this.adminVideoService.findActors(this.Query, this.Status, this.Page, this.Limit)
      : this.type === 'tags'
        ? await this.adminVideoService.findTags(this.Query, this.Status, this.Page, this.Limit)
        : await this.adminVideoService.findCategories(this.Query, this.Status, this.Page, this.Limit);
    this.rows = rpt.Data?.Data || [];
    this.TotalRows = rpt.Data?.TotalRows || 0;
    this.Page = rpt.Data?.Page || this.Page;
    this.Limit = rpt.Data?.Limit || this.Limit;
    this.pageInput = this.Page;
    if (syncUrl) {
      await this.syncQueryParams();
    }
  }

  async search(): Promise<void> {
    await this.findAll(1);
  }

  private async syncQueryParams(): Promise<void> {
    await this.router.navigate([], {
      relativeTo: this.route,
      replaceUrl: true,
      queryParams: {
        Page: this.Page,
        Limit: this.Limit,
        Query: this.Query || null,
        Status: this.Status || null
      },
      queryParamsHandling: 'merge'
    });
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.TotalRows / this.Limit));
  }

  canPreviousPage(): boolean {
    return this.Page > 1;
  }

  canNextPage(): boolean {
    return this.Page < this.totalPages();
  }

  async goToFirstPage(): Promise<void> {
    await this.findAll(1);
  }

  async goToLastPage(): Promise<void> {
    await this.findAll(this.totalPages());
  }

  async goToPageInput(): Promise<void> {
    const requestedPage = Number(this.pageInput);
    if (!Number.isFinite(requestedPage)) {
      this.pageInput = this.Page;
      return;
    }
    const targetPage = Math.trunc(requestedPage);
    if (targetPage < 1 || targetPage > this.totalPages()) {
      this.pageInput = this.Page;
      return;
    }
    await this.findAll(targetPage);
  }

  async save(): Promise<void> {
    this.errorMessage = '';
    this.saveMessage = '';
    const rpt = this.type === 'actors'
      ? await this.adminVideoService.saveActor(this.entity)
      : this.type === 'tags'
        ? await this.adminVideoService.saveTag(this.entity)
        : await this.adminVideoService.saveCategory(this.entity);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      this.showSaveMessage(rpt.Message || 'No se pudo guardar el registro.', 'error');
      return;
    }
    this.entity = { ...rpt.Data };
    this.showSaveMessage('Registro guardado correctamente.', 'success');
    await this.findAll(this.Page);
  }

  showSaveMessage(message: string, type: 'success' | 'error'): void {
    this.saveMessage = message;
    this.saveMessageType = type;
  }

  closeSaveMessage(): void {
    this.saveMessage = '';
  }

  async enable(row: any): Promise<void> {
    if (this.type === 'actors') await this.adminVideoService.enableActor(row);
    else if (this.type === 'tags') await this.adminVideoService.enableTag(row);
    else await this.adminVideoService.enableCategory(row);
    await this.findAll(this.Page);
  }

  async disable(row: any): Promise<void> {
    if (this.type === 'actors') await this.adminVideoService.disableActor(row);
    else if (this.type === 'tags') await this.adminVideoService.disableTag(row);
    else await this.adminVideoService.disableCategory(row);
    await this.findAll(this.Page);
  }
}
