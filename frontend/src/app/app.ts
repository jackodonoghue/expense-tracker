import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Expense, ExpenseService } from './services/expense';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrls: ['./app.scss']
})
export class App implements OnInit {
  private expenses: Expense[] = [];
  private expenseService = inject(ExpenseService);

  ngOnInit() {
    this.loadExpenses();
  }

  loadExpenses() {
    this.expenseService.getExpenses().subscribe(data => {
    this.expenses = data;
    });
  }

  addDummy() {
   const dummy: Expense = { description: 'Coffee', amount: 3.50 };
   this.expenseService.addExpense(dummy).subscribe(() => this.loadExpenses());
  }
}
