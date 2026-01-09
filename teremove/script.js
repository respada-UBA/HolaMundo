// Simple calculator logic (client-side)
const display = document.getElementById('display');
let first = null;
let operator = null;
let waitingForSecond = false;

function updateDisplay(text) {
  display.textContent = text;
}

function inputDigit(digit) {
  if (waitingForSecond) {
    updateDisplay(digit === '.' ? '0.' : digit);
    waitingForSecond = false;
  } else {
    const cur = display.textContent === '0' && digit !== '.' ? digit : display.textContent + digit;
    updateDisplay(cur);
  }
}

function handleOperator(nextOp) {
  const inputValue = parseFloat(display.textContent);
  if (first === null) {
    first = inputValue;
  } else if (operator) {
    const result = operate(first, inputValue, operator);
    updateDisplay(String(result));
    first = result;
  }
  operator = nextOp;
  waitingForSecond = true;
}

function operate(a, b, op) {
  if (op === '+') return a + b;
  if (op === '-') return a - b;
  if (op === '*') return a * b;
  if (op === '/') return b === 0 ? 'Error' : a / b;
  return b;
}

document.querySelector('.keys').addEventListener('click', (e) => {
  const t = e.target;
  if (!t.matches('button')) return;

  if (t.dataset.number !== undefined) {
    inputDigit(t.dataset.number);
    return;
  }
  const action = t.dataset.action;
  if (!action) return;

  if (action === 'clear') {
    first = null; operator = null; waitingForSecond = false;
    updateDisplay('0');
    return;
  }
  if (action === 'posneg') {
    const val = parseFloat(display.textContent);
    updateDisplay(String(-val));
    return;
  }
  if (action === 'percent') {
    const val = parseFloat(display.textContent);
    updateDisplay(String(val / 100));
    return;
  }
  if (action === '=') {
    if (operator === null || first === null) return;
    const result = operate(first, parseFloat(display.textContent), operator);
    updateDisplay(String(result));
    first = null; operator = null; waitingForSecond = true;
    return;
  }
  // otherwise it's an operator symbol like + - * /
  handleOperator(action);
});
