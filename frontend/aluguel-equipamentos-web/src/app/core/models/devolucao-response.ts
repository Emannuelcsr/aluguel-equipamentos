import { ItemDevolucaoResponse } from "./item-devolucao-response";

export interface DevolucaoResponse {

  id: number;
  reservaId : number;  
  dataHoraDevolucao: string;  
  usuarioResponsavelId: number;  
  usuarioResponsavelNome:string
  itens:ItemDevolucaoResponse[];
}
