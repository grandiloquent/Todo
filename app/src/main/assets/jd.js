(function() {
    var Jd = function Jd() {};
    Jd.prototype.initialize = function() {
        this.hideItems();
        var that = this;
        window.addEventListener('scroll', function(event) {
            that.hideItems();
        });
    }
    Jd.prototype.hideItems = function() {

        var elements = document.querySelectorAll('.search_interlude');
        for (var i = elements.length - 1; i >= 0; i--) {
            var element = elements[i];
            if (element) {
                element.style.display = 'none';
            }
        };

        var items = document.querySelectorAll('.search_prolist_item');

        for (var i = items.length - 1; i >= 0; i--) {
            var item = items[i];
            var element = item.querySelector('.search_prolist_other .mod_tag img');
            if (!element) {
                item.parentNode.removeChild(item);
            } else {
                var src = element.getAttribute('src');
                if (!src || !src.endsWith('c5ab4d78f8bf4d90.png')) {
                    item.style.display = "none";
                    item.parentNode.removeChild(item);
                }
            }

        }
    };

    var jd = new Jd();
    jd.initialize();
})();