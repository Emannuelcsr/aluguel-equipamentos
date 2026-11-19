import { Component, OnInit } from '@angular/core';
import { ReservaResponse } from '../../../core/models/reserva-response';
import {
  FormControl,
  FormGroup,
  NgSelectOption,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { ReservaService } from '../service/reserva.service';
import { CadastrarReservaRequest } from '../../../core/models/cadastrar-reserva-request';
import { AbstractControl, ValidationErrors } from '@angular/forms';
import { UnidadeEquipamentoResponse } from '../../../core/models/unidade-equipamento-response';
import { UnidadeEquipamentoService } from '../../unidade-equipamento/service/unidade-equipamento.service';
import { ClienteService } from '../../clientes/service/cliente.service';
import { ClienteResumoResponse } from '../../../core/models/cliente-resumo-response';

function validarPeriodo(controle: AbstractControl): ValidationErrors | null {
  const dataInicial = controle.get('dataInicial')?.value;
  const dataFinal = controle.get('dataFinal')?.value;

  if (dataInicial && dataFinal && dataFinal < dataInicial) {
    return { periodoInvalido: true };
  }

  return null;
}
@Component({
  selector: 'app-cadastro-reserva',
  imports: [ReactiveFormsModule],
  templateUrl: './cadastro-reserva.component.html',
  styleUrl: './cadastro-reserva.component.scss',
})
export class CadastroReservaComponent implements OnInit {
  mensagemErro = '';
  mensagemSucesso = '';
  criando = false;
  reservas: ReservaResponse[] = [];
  unidades: UnidadeEquipamentoResponse[] = [];
  listando = false;
  clientes: ClienteResumoResponse[] = [];
  carregandoClientes =false;

  constructor(
    private router: Router,
    private reservaService: ReservaService,
    private unidadeEquipamentoService: UnidadeEquipamentoService,
    private clienteService:ClienteService,
  ) {}
  ngOnInit(): void {
    this.carregarUnidades();
    this.carregarClientes();
  }

  formularioCadastro = new FormGroup(
    {
      clienteId: new FormControl<number | null>(null, {
        validators: [Validators.required],
      }),
      dataInicial: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      dataFinal: new FormControl('', {
        nonNullable: true,
        validators: [Validators.required],
      }),
      unidadesIds: new FormControl<number[]>([], {
        nonNullable: true,
        validators: [Validators.required, Validators.minLength(1)],
      }),
    },
    {
      validators: validarPeriodo,
    },
  );

  carregarUnidades() {
    if (this.listando) {
      return;
    }

    this.listando = true;
    this.unidadeEquipamentoService.listarDisponiveis().subscribe({
      next: (resposta) => {
        this.listando = false;
        this.unidades = resposta;
      },
      error: (erro) => {
        this.listando = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível carregar as unidades de equipamento.';
      },
    });
  }

carregarClientes(){
   if (this.carregandoClientes) {
      return;
    }

    this.carregandoClientes  = true;
    this.clienteService.listarAtivos().subscribe({
      next:(resposta)=>{
       this.carregandoClientes  = false;
        this.clientes = resposta;

      },
      error:(erro)=>{
        this.carregandoClientes  = false;
        if (erro.status === 0) {
          this.mensagemErro =
            'Não foi possível conectar ao servidor. Verifique a conexão e tente novamente.';
          return;
        }

        this.mensagemErro =
          erro.error?.mensagens?.[0] ??
          'Não foi possível carregar os clientes.';

      }


    });
}

  alterarSelecaoUnidade(id: number, evento: Event) {
    const checkbox = evento.target as HTMLInputElement;
    const marcado = checkbox.checked;

    const unidadesSelecionadas =
      this.formularioCadastro.controls.unidadesIds.value;

    if (marcado && !unidadesSelecionadas.includes(id)) {
      this.formularioCadastro.controls.unidadesIds.setValue([
        ...unidadesSelecionadas,
        id,
      ]);
    }
    if (!marcado) {

      const unidadesAtualizadas = unidadesSelecionadas.filter(
          unidadeId => unidadeId !== id
      );

      this.formularioCadastro.controls.unidadesIds.setValue(unidadesAtualizadas);
    }

    
  }



  cadastrar() {
    if (this.criando) {
      return;
    }

    if (this.formularioCadastro.invalid) {
      this.formularioCadastro.markAllAsTouched();
      return;
    }

    const dadosCadastro = this.formularioCadastro.getRawValue();


    
    if (dadosCadastro.clienteId === null) {
      return;
    }

    const reserva: CadastrarReservaRequest = {
      clienteId: dadosCadastro.clienteId,
      dataInicial: dadosCadastro.dataInicial,
      dataFinal: dadosCadastro.dataFinal,
      unidadesIds: dadosCadastro.unidadesIds,
    };

    this.mensagemErro = '';
    this.mensagemSucesso = '';
    this.criando = true;

    this.reservaService.cadastrar(reserva).subscribe({
      next: (resposta) => {
        this.criando = false;

        this.router.navigate(['/reservas'], {
          state: {
            mensagemSucesso: 'Reserva cadastrada com sucesso!',
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
          erro.error?.mensagens?.[0] ?? 'Não foi possível cadastrar a reserva.';
      },
    });
  }
}
