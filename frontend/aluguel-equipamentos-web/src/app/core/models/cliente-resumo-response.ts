export interface ClienteResumoResponse {
  id: number;
  tipo: 'PF' | 'PJ';
  nomeRazaoSocial: string;
  nomeFantasia: string | null;
  documento: string;
  email: string;
  telefone: string;  
  ativo: boolean;
  }
