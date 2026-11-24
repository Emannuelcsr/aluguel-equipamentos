
import { MetodoVerificacaoRetirada } from "./metodo-verificacao-retirada.enum";
import { ItemRetiradaRequest } from "./item-retirada-request";

export interface CadastrarRetiradaRequest {
  reservaId: number;
  documentoRecebedor: string;
  nomeRecebedor: string;
  metodoVerificacao:MetodoVerificacaoRetirada;
  descricaoOutroMetodo: string | null;
  itens:ItemRetiradaRequest[];
  autorizacaoConfirmada:boolean;
  recebimentoConfirmado:boolean;
}