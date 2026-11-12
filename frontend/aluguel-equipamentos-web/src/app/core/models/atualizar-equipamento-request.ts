export interface AtualizarEquipamentoRequest {
  nome: string;
  descricao: string | null;
  valorDiaria: number;
  categoriaId: number;
}
