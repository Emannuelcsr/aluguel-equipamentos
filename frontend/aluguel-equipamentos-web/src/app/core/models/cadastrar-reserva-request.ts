export interface CadastrarReservaRequest {
  clienteId: number;
  dataInicial: string;
  dataFinal: string;
  unidadesIds: number[];
}