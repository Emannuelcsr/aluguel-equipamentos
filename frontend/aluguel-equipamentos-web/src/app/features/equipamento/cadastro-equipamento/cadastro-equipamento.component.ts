import { Component, OnInit } from '@angular/core';
import { CategoriaService } from '../../categoria/service/categoria.service';
import { EquipamentoService } from '../service/equipamento.service';
import { Router, RouterLink } from '@angular/router';
import {
  FormControl,
  FormGroup,
  NgSelectOption,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CategoriaResponse } from '../../../core/models/categoria-response';
import { CadastrarEquipamentoRequest } from '../../../core/models/cadastrar-equipamento-request';

@Component({
  selector: 'app-cadastro-equipamento',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './cadastro-equipamento.component.html',
  styleUrl: './cadastro-equipamento.component.scss',
})
export class CadastroEquipamentoComponent implements OnInit {
  mensagemErro = '';
  mensagemSucesso = '';
  criando = false;
  categorias: CategoriaResponse[] = [];

  constructor(
    private categoriaService: CategoriaService,
    private equipamentoService: EquipamentoService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.categoriaService.listarAtivas().subscribe({
      next: (resposta) => {
        this.categorias = resposta;
      },
      error: (erro) => {
        
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível carregar as categorias.';
      },
    });
  }

  formularioCadastro = new FormGroup({
    nome: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(150),
      ],
    }),
    descricao: new FormControl('', {
      nonNullable: true,
      validators: [Validators.minLength(3), Validators.maxLength(500)],
    }),
    valorDiaria: new FormControl<number | null>(null, {
      validators: [
        Validators.required,
        Validators.min(1),
        Validators.max(100000),
      ],
    }),
    categoriaId: new FormControl<number | null>(null, {
      validators: [Validators.required],
    }),
  });

  cadastrar() {
    if (this.criando) {
      return;
    }

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.formularioCadastro.getRawValue();

    if (
      dadosCadastro.valorDiaria === null ||
      dadosCadastro.categoriaId === null
    ) {
      return;
    }

    const descricao = dadosCadastro.descricao.trim() || null;

    const equipamento: CadastrarEquipamentoRequest = {
      nome: dadosCadastro.nome,
      descricao: descricao,
      valorDiaria: dadosCadastro.valorDiaria,
      categoriaId: dadosCadastro.categoriaId,
    };

    this.mensagemErro = '';
    this.criando = true;
    this.equipamentoService.cadastrar(equipamento).subscribe({
      next: (resposta) => {
        this.criando = false;

        this.router.navigate(['/equipamentos'], {
          state: {
            mensagemSucesso: 'Equipamento cadastrado com sucesso!',
          },
        });
      },
      error: (erro) => {
        this.criando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível cadastrar o equipamento.';
      },
    });
  }
}
