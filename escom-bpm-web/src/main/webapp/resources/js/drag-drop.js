function initDND() {                        //строки дерева источник
    $('.ui-treenode .dragable').draggable({
        scope: 'test',
        zIndex: ++PrimeFaces.zindex,
        helper: 'clone'
    });

    $('.ui-datatable .dropoint').droppable({//строки таблицы приёмники
        hoverClass: 'hoverClass',
        tolerance: 'touch',
        scope: 'test',
        drop: function (event, ui) {
            var draggableId = $(ui.draggable).attr('id');
            var droppableId = $(this).attr('id');
            var droppedItem = $(ui.draggable);
            droppedItem.fadeOut('slow');

            dropToTable([
                {name: 'dragId', value: draggableId},
                {name: 'dropId', value: droppableId}
            ]);
        }
    });

    $('.ui-datatable .dropoint').draggable({//строка таблицы источник                     
        scope: 'test',
        helper: function () {
            var th = $(this);
            return th.clone().appendTo(document.body).css('width', th.width());
        }
    });

    $('.ui-tree .ui-treenode-label').droppable({//строки дерева приёмники
        helper: 'clone',
        scope: 'test',
        hoverClass: 'hoverClass',
        tolerance: 'pointer',
        drop: function (event, ui) {
            var draggableId = $(ui.draggable).attr("id");
            var droppableId = $(this).parent().parent().attr("id");
            var droppedItem = $(ui.draggable);

            droppedItem.fadeOut('slow');

            dropToTree([
                {name: 'dragId', value: draggableId},
                {name: 'dropId', value: droppableId}
            ]);
        }
    });

    $('.btnNavigator').droppable({//строки дерева приёмники
        helper: 'clone',
        scope: 'test',
        activeClass: 'ui-state-active',
        hoverClass: 'ui-state-highlight',
        tolerance: 'pointer',
        drop: function (event, ui) {
            var draggableId = $(ui.draggable).attr("id");
            var droppableId = $(this).attr("id");
            var droppedItem = $(ui.draggable);

            droppedItem.fadeOut('slow');

            dropToNavig([
                {name: 'dragId', value: draggableId},
                {name: 'dropId', value: droppableId}
            ]);
        }
    });

}

$(function () {
    initDND();
});



