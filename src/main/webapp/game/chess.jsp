<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<t:layout page_name="Szachy">
    <jsp:attribute name="head">
    <link rel="stylesheet"
          href="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.css"
          integrity="sha384-q94+BZtLrkL1/ohfjR8c6L+A6qzNH9R2hBLwyoAfu3i/WCvQjzL2RQJ3uNHDISdU"
          crossorigin="anonymous">

    <script src="https://code.jquery.com/jquery-3.5.1.min.js"
            integrity="sha384-ZvpUoO/+PpLXR1lu4jmpXWu80pZlYUAfxl5NsBMWOEPSjUn/6Z/hRTt8+pR6L4N2"
            crossorigin="anonymous"></script>
    <script src="https://unpkg.com/@chrisoakman/chessboardjs@1.0.0/dist/chessboard-1.0.0.min.js"
            integrity="sha384-8Vi8VHwn3vjQ9eUHUxex3JSN/NFqUg3QbPyX8kWyb93+8AC/pPWTzj+nHtbC5bxD"
            crossorigin="anonymous"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/chess.js/0.10.3/chess.min.js"></script>

    <script>
        // skrypty
    </script>
    <style>
        main {
            display: grid;
            place-items: center;
            flex: 1 1 auto !important;
            align-content: stretch;
        }
        #board {
            aspect-ratio: 1 / 1;
            height: 70vh;
            width: 70vh;
            max-height: 70vh;
            max-width: 70vh;
        }
    </style>
    </jsp:attribute>

    <jsp:attribute name="body">
    <main class="site-margin">
            <div id="board"></div>
        <script>
            var board = null
            var game = new Chess()
            
            function onDragStart (source, piece, position, orientation) {
              // do not pick up pieces if the game is over
              if (game.game_over()) return false
            
              // only pick up pieces for White
              if (piece.search(/^b/) !== -1) return false
            }
            
            function makeRandomMove () {
              var possibleMoves = game.moves()
            
              // game over
              if (possibleMoves.length === 0) return
            
              var randomIdx = Math.floor(Math.random() * possibleMoves.length)
              game.move(possibleMoves[randomIdx])
              board.position(game.fen())
            }
            
            function onDrop (source, target) {
              // see if the move is legal
              var move = game.move({
                from: source,
                to: target,
                promotion: 'q' // NOTE: always promote to a queen for example simplicity
              })
            
              // illegal move
              if (move === null) return 'snapback'
            
              // make random legal move for black
              window.setTimeout(makeRandomMove, 250)
            }
            
            // update the board position after the piece snap
            // for castling, en passant, pawn promotion
            function onSnapEnd () {
              board.position(game.fen())
            }
            
            var config = {
              draggable: true,
              position: 'start',
              onDragStart: onDragStart,
              onDrop: onDrop,
              onSnapEnd: onSnapEnd
            }
            board = Chessboard('board', config)
        </script>
    </main>
    </jsp:attribute>
</t:layout>
