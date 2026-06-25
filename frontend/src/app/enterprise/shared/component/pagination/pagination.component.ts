import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { PaginationDto } from '../../model/dto/PaginationDto';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.component.html'
})
export class PaginationComponent implements OnChanges {
  @Input() pagination: PaginationDto = new PaginationDto();
  @Output() pageChange = new EventEmitter<number>();

  pageInput = 1;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['pagination']) {
      this.pageInput = this.currentPage();
    }
  }

  shouldShow(): boolean {
    return this.totalRows() > this.limit();
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.totalRows() / this.limit()));
  }

  canPreviousPage(): boolean {
    return this.currentPage() > 1;
  }

  canNextPage(): boolean {
    return this.currentPage() < this.totalPages();
  }

  goToFirstPage(): void {
    this.emitPage(1);
  }

  goToPreviousPage(): void {
    this.emitPage(this.currentPage() - 1);
  }

  goToNextPage(): void {
    this.emitPage(this.currentPage() + 1);
  }

  goToLastPage(): void {
    this.emitPage(this.totalPages());
  }

  goToPageInput(): void {
    const targetPage = Math.trunc(Number(this.pageInput));
    if (!Number.isFinite(targetPage) || targetPage < 1 || targetPage > this.totalPages()) {
      this.pageInput = this.currentPage();
      return;
    }
    this.emitPage(targetPage);
  }

  itemLabel(): string {
    return this.pagination?.ItemLabel || 'registros';
  }

  private emitPage(page: number): void {
    const targetPage = Math.min(this.totalPages(), Math.max(1, page));
    if (targetPage === this.currentPage()) {
      this.pageInput = this.currentPage();
      return;
    }
    this.pageChange.emit(targetPage);
  }

  private currentPage(): number {
    return Math.max(1, Number(this.pagination?.Page) || 1);
  }

  private limit(): number {
    return Math.max(1, Number(this.pagination?.Limit) || 1);
  }

  private totalRows(): number {
    return Math.max(0, Number(this.pagination?.TotalRows) || 0);
  }
}
