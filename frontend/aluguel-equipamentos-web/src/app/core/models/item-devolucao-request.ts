import { CondicaoFisicaDevolucao } from "./condicao-fisica-devolucao.enum";
import { CondicaoFisicaRetirada } from "./condicao-fisica-enum";


export interface ItemDevolucaoRequest {

    itemReservaId:number;
    condicaoFisica:CondicaoFisicaDevolucao;
    observacoes:string | null;
}
