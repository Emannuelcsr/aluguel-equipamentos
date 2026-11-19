import { ItemReservaResponse } from "./Item-reserva-response";

export interface ReservaResponse {

  id: number;
  clienteId : number;  
  clienteNome: string;  
  dataInicial: string;
  dataFinal:string;
  status: string;
  valorTotal: number;
  itens:ItemReservaResponse[];
  dataCriacao:string
  dataAtualizacao:string;
}
