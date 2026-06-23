import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  Page = 1;
  Limit = 20;
  TotalRows = 0;

  constructor(private route: ActivatedRoute, private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    this.route.url.subscribe(async segments => {
      this.type = segments[1]?.path || 'categories';
      this.newEntity();
      await this.findAll(1);
    });
  }

  title(): string {
    if (this.type === 'actors') return 'Actores';
    if (this.type === 'tags') return 'Tags';
    return 'Categorias';
  }

  newEntity(): void {
    if (this.type === 'actors') this.entity = new ActorEntity();
    else if (this.type === 'tags') this.entity = new TagEntity();
    else this.entity = new VideoCategoryEntity();
  }

  edit(row: any): void {
    this.entity = { ...row };
  }

  async findAll(page: number = this.Page): Promise<void> {
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
  }

  async search(): Promise<void> {
    await this.findAll(1);
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

  async save(): Promise<void> {
    this.errorMessage = '';
    const rpt = this.type === 'actors'
      ? await this.adminVideoService.saveActor(this.entity)
      : this.type === 'tags'
        ? await this.adminVideoService.saveTag(this.entity)
        : await this.adminVideoService.saveCategory(this.entity);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.newEntity();
    await this.findAll(this.Page);
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
