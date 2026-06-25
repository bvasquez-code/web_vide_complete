export class PaginationDto {
  Page = 1;
  Limit = 20;
  TotalRows = 0;
  ItemLabel = 'registros';

  constructor(init?: Partial<PaginationDto>) {
    Object.assign(this, init || {});
  }
}
