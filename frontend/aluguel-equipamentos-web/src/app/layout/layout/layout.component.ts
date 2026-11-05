import { Component, OnInit } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from '@angular/router';
import { AutenticacaoService } from '../../core/services/autenticacao.service';
import { UsuarioAutenticadoResponse } from '../../core/models/usuario-autenticado-response';

@Component({
  selector: 'app-layout',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.scss',
})
export class LayoutComponent  implements OnInit{
  usuario: UsuarioAutenticadoResponse | null = null;

  constructor(
    private autenticacaoService: AutenticacaoService,
    private router: Router,
  ) {}

ngOnInit(): void {
  this.usuario = this.autenticacaoService.obterUsuario();
}

  sair(): void {
    this.autenticacaoService.logout();
    this.router.navigate(['/login']);
  }
}
