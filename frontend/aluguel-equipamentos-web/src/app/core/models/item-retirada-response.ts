import { CondicaoFisicaRetirada } from "./condicao-fisica-enum";

export interface ItemRetiradaResponse {

  id: number;
  itemReservaId : number;  
  unidadeEquipamentoId: number;  
  codigoUnidade: string;
  equipamentoNome:string;
  condicaoFisica: CondicaoFisicaRetirada;
  observacoes: string | null;

}
