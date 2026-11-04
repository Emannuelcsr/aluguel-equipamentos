export interface PaginaResponse<T> {

  conteudo: T[];

  paginaAtual: number;

  tamanho: number;

  quantidadeElementos: number;

  totalElementos: number;

  totalPaginas: number;

  primeiraPagina: boolean;

  ultimaPagina: boolean;

}