import { ItemDevolucaoRequest } from "./item-devolucao-request";

export interface CadastrarDevolucaoRequest {
  reservaId: number;
  itens:ItemDevolucaoRequest[];
  confirmacaoRecebimento:boolean;
}