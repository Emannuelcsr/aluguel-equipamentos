import { CondicaoFisicaRetirada } from "./condicao-fisica-enum";


export interface ItemRetiradaRequest {

    itemReservaId:number;
    condicaoFisica:CondicaoFisicaRetirada;
    observacoes:string | null;
}
