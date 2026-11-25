import { CondicaoFisicaRetirada } from "./condicao-fisica-enum";

export interface ItemDevolucaoResponse {

  id: number;
  itemReservaId : number;  
  unidadeEquipamentoId: number;  
  codigoUnidade: string;
  equipamentoNome:string;
  condicaoFisica: CondicaoFisicaRetirada;
  observacoes: string | null;

}
