'use strict';

/**
 * @ngdoc function
 * @name jsApp.controller:MainCtrl
 * @description
 * # MainCtrl
 * Controller of the jsApp
 */

function fromWebSocket(address, protocol) {
    
    var ws = new WebSocket(address, protocol);
    ws.onopen = function() { 
        ws.send('GameInfo'); 
    };
    var observable = Rx.Observable.fromEvent(ws, 'message').map(function(evt) { 
        console.log("Observable: " + evt.data);
        return JSON.parse(evt.data)
    });
    
    var observer = Rx.Observer.fromNotifier(function(msg) {
        if(msg.kind == 'N') {
            console.log("Observer: notify " + msg.value)
            ws.send(msg.value); 
        } else {
            console.log("notify type " + msg.kind)
        }        
    });
    
    observable.subscribe(
            function(msg) { console.log(msg); }, 
            function(error) { console.log(error);}, 
            function() { console.log("close ws observable");ws.close();});
            
    var subject = new Rx.Subject.create(observer, observable)
    return subject;
}

angular.module('jsApp',['rx','googlechart'])
  .controller('MainCtrl', function ($scope,observeOnScope) {         
      
      $scope.players = { current:[] };
      $scope.winner = null;
      $scope.error = null
      $scope.board = {
          rows: 0,
          columns: 0,
          cell:[]
      }
    $scope.model = {name:"", player:""};
    
    var gameSubject = fromWebSocket('ws://127.0.0.1:9001/trifinity', []);
    
    gameSubject.subscribe(function(data) {
        console.log(data)
    })
    
    gameSubject.filter(function(data){ 
            return typeof(data['board'])=='object'; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.board = data.board;                    
            });
        });
        
     gameSubject.filter(function(data){ 
         return typeof(data.players)=='object'; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.players.current = data.players;    
            });
        });
        
        gameSubject.filter(function(data){ 
         return typeof(data.winners)=='object' && data.winners.length > 0; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.winner = data.winners[0];    
            });
        });

        
     gameSubject.filter(function(data){ 
         return data.currentPlayer; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.players.turn = data.currentPlayer;
            });
        });
             
        
     gameSubject.filter(function(data){ 
         return typeof(data.error)=='string'; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.errorMessage = data.error;    
            });
        });
        
     gameSubject.filter(function(data){ 
         console.log(typeof(data.notification))
         return typeof(data.notification)=='string'; 
        })
        .subscribe(function(data) {
            $scope.$apply(function(){
                $scope.notificationMessage = data.notification;    
            });
        });
        


    $scope.sendJoinCmd = function() {
        gameSubject.onNext('Join ' + $scope.model.player)
    }
    
    $scope.sendLeaveCmd = function() {
        gameSubject.onNext('Leave Anonymous')
    }
    
    $scope.sendNewGameCmd = function() {
        gameSubject.onNext('NewGame')
    }
    
    $scope.sendMoveCmd = function(x,y) {
        gameSubject.onNext('Move ' + x + ' ' + y)
    }
        
  })
.directive('triBoard', function() {
  return {
    restrict: 'E',
    templateUrl: 'views/directives/board.html',
    transclude:true,
    scope: '=',
    controller: function($scope) {
        $scope.$watch('board', function() {
            var board = $scope.board
            var cells = {}
            for(var r = 0; r < board.rows; r ++) {    
                cells['row_' + r] = {}
                for(var c = 0 ; c < board.columns; c++ ) {
                    var value = board.cell[r * board.columns + c];
                    cells['row_' + r]['cell' + r + '_' + c] = [value , r, c]; 
                }
            }
            $scope.cells = cells;
        }, true);
        
        $scope.move = function(r,c) {
            var cell = $scope.cells[r][c];
            $scope.sendMoveCmd(cell[1],cell[2]);
        }
    }
  };
});
