import { CondicaoFisicaDevolucao } from "./condicao-fisica-devolucao.enum";
import { CondicaoFisicaRetirada } from "./condicao-fisica-enum";

export interface ItemDevolucaoResponse {

  id: number;
  itemReservaId : number;  
  unidadeEquipamentoId: number;  
  codigoUnidade: string;
  equipamentoNome:string;
  condicaoFisica: CondicaoFisicaDevolucao;
  observacoes: string | null;

}
