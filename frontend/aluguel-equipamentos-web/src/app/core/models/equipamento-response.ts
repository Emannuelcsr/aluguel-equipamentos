export interface EquipamentoResponse {
  id: number;
  nome: string;
  descricao: string | null;
  categoriaId: number;
  categoriaNome: string;
  valorDiaria: number;
  ativo: boolean;
  dataCriacao: string;
  dataAtualizacao: string;
}
