import {test} from 'node:test';
import assert from 'node:assert/strict';
import {currencyFor,parseAmount,balance,notificationRoute} from '../src/domain/format.ts';
test('country currencies',()=>{assert.equal(currencyFor('EG'),'EGP');assert.equal(currencyFor('JO'),'JOD')});
test('exact minor units and Arabic digits',()=>{assert.equal(parseAmount('٥٫١٢','EGP'),512);assert.equal(parseAmount('5.123','JOD'),5123);assert.throws(()=>parseAmount('5.123','EGP'));assert.throws(()=>parseAmount('-2','JOD'));assert.throws(()=>parseAmount('0','JOD'))});
test('balances never combine currencies',()=>assert.equal(balance([{currency:'EGP',minor:500,kind:'income'},{currency:'JOD',minor:400,kind:'income'},{currency:'EGP',minor:100,kind:'expense'}],'EGP'),400));
test('notification paths are allowlisted',()=>{assert.equal(notificationRoute({type:'prayer'}),'/prayer');assert.equal(notificationRoute({type:'https://bad.example'}),null);assert.equal(notificationRoute({type:'task',id:'../admin'}),null)});
