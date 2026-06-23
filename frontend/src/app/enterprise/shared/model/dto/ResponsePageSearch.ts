export class ResponsePageSearch<T> {
  Data: T[] = [];
  TotalRows: number = 0;
  Page: number = 1;
  Limit: number = 10;
}
