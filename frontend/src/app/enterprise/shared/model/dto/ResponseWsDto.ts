export class ResponseWsDto {
  Status: string = '';
  Message: string = '';
  Data: any;
  ErrorStatus: boolean = false;
  ErrorID: string = '';
  DataAdditional: any = {};
}
