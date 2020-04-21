if (typeof(Wicket) == "undefined") {
    Wicket = { };
}

if (Wicket.Class == null) {
    Wicket.Class = {
        create: function() {
            return function() {
                this.initialize.apply(this, arguments);
            };
        }
    };
}

if (Wicket.Object == null) {
    Wicket.Object = { };
}

if (Wicket.Object.extend == null) {
    Wicket.Object.extend = function(destination, source) {
        for (property in source) {
            destination[property] = source[property];
        }
        return destination;
    };
}


/**
 * Draggable (and optionally resizable) window that can either hold a div
 * or an iframe.
 */
Wicket.Window = Wicket.Class.create();

/**
 * Display confirmation dialog if the user is about to leave a page (IE and FF).
 */
Wicket.Window.unloadConfirmation = false;

/**
 * Creates a wicket window instance. The advantage of using this is
 * that in case an iframe modal window is opened in an already displayed
 * iframe modal window, the new window is created as a top-level window.
 *
 */
Wicket.Window.create = function(settings) {
    var win;

    // if it is an iframe window...
    if (typeof(settings.src) != "undefined" && !Wicket.Browser.isKHTML()) {
        // attempt to get class crom parent
        try {
            win = window.parent.Wicket.Window;
        } catch (ignore) {
        }
    }

    // no parent...
    if (typeof(win) == "undefined") {
        win = Wicket.Window;
    }

    // create and return instance
    return new win(settings);
};

/**
 * Returns the current top level window (null if none).
 */
Wicket.Window.get = function() {
    var win = null;

    if (typeof(Wicket.Window.current) != "undefined") {
        win = Wicket.Window.current;
    }
    else {
        try {
            win = window.parent.Wicket.Window.current;
        } catch (ignore) {
        }
    }
    return win;
};

/**
 * Closes the current wicket open window. This method is supposed to
 * be called from inside the window (therefore it checks window.parent).
 */
Wicket.Window.close = function() {
    var win;
    try {
        win = window.parent.Wicket.Window;
    } catch (ignore) {
    }

    if (typeof(win) != "undefined" && typeof(win.current) != "undefined" && win.current != null) {
        // we can't call close directly, because it will delete our window,
        // so we will schedule it as timeout for parent's window
        window.parent.setTimeout(function() {
            win.current.close();
        }, 0);

        return true;
    }
    else {
        return false;
    }
};

Wicket.Window.prototype = {

    /**
     * Creates a new window instance.
     * Note:
     *   Width refers to the width of entire window (including frame).
     *   Height refers to the height of user content.
     *
     * @param {Object} settings - map that contains window settings. the default
     *                            values are below - together with description
     */
    initialize: function(settings) {

        // override default settings with user settings
        this.settings = Wicket.Object.extend({

            minWidth: 200,  /* valid only if resizable */
            minHeight: 150, /* valid only if resizable */

            className: "w_blue", /* w_silver */

            width: 600,  /* initial width */
            height: 300, /* may be null for non-iframe, non-resizable window (automatic height) */

            resizable: true,

            widthUnit: "px", /* valid only if not resizable */
            heightUnit: "px", /* valid only if not resizable */

            src: null,     /* iframe src - this takes precedence over the "element" property */
            element: null, /* content element (for non-iframe window) */

            iframeName: null, /* name of the iframe */

            cookieId: null, /* id of position (and size if resizable) cookie */

            title: null, /* window title. if null and window content is iframe, title of iframe document will be used. */

            onCloseButton: function() {
                /* On firefox on Linux, at least, we need to blur() textfields, etc.
                 * to get it to update its DOM model. Otherwise you'll lose any changes
                 * made to the current form component you're editing.
                 */
                $(this.window).find("button.close")[0].focus();
                $(this.window).find("button.close")[0].blur();
                this.close();
                return false;
            }.bind(this), /* called when close button is clicked */

            onClose: function() {
            }, /* called when window is closed */

            mask: "semi-transparent" /* or "transparent" */

        }, settings || { });

    },

    /**
     * Creates the DOM elements of the window.
     */
    createDOM: function() {
        var idWindow = this.newId();
        var idClassElement = this.newId();
        var idCaption = this.newId();
        var idFrame = this.newId();
        var idTop = this.newId();
        var idTopLeft = this.newId();
        var idTopRight = this.newId();
        var idLeft = this.newId();
        var idRight = this.newId();
        var idBottomLeft = this.newId();
        var idBottomRight = this.newId();
        var idBottom = this.newId();
        var idCaptionText = this.newId();

        var markup = Wicket.Window.getMarkup(idWindow, idClassElement, idCaption, idFrame,
            idTop, idTopLeft, idTopRight, idLeft, idRight, idBottomLeft, idBottomRight,
            idBottom, idCaptionText);

        $(document.body).append(markup);

        var _ = function(name) {
            return document.getElementById(name);
        };

        this.window = _(idWindow);
        this.classElement = _(idClassElement);
        this.caption = _(idCaption);
        this.content = _(idFrame);
        this.top = _(idTop);
        this.topLeft = _(idTopLeft);
        this.topRight = _(idTopRight);
        this.left = _(idLeft);
        this.right = _(idRight);
        this.bottomLeft = _(idBottomLeft);
        this.bottomRight = _(idBottomRight);
        this.bottom = _(idBottom);
        this.captionText = _(idCaptionText);
    },

    /**
     * Creates the new uniqe id for window element.
     */
    newId: function() {
        return "_wicket_window_" + Wicket.Window.idCounter++;
    },

    /**
     * Returns the content document
     */
    getContentDocument: function() {
        if (this.isIframe()) {
            return this.content.contentWindow.document;
        }
        else {
            return document;
        }
    },

    show: function() {
        // create the DOM elements
        this.createDOM();

        $(this.window).modal({
            backdrop: 'static',
            keyboard: false
        });
        // it's an element content

        // is the element specified?
        if (this.settings.element == null) {
            throw "Either src or element must be set.";
        }

        if (this.settings.title != null)
            this.captionText.innerHTML = this.settings.title;

        // reparent the element
        this.oldParent = this.settings.element.parentNode;
        this.settings.element.parentNode.removeChild(this.settings.element);
        this.content.appendChild(this.settings.element);

        this.content.style.overflow = "auto";

        $(this.window).find("button.close")[0].onclick = this.settings.onCloseButton.bind(this);

        // is there a window displayed already?
        if (Wicket.Window.current != null) {
            // save the reference to it
            this.oldWindow = Wicket.Window.current;
        }
        // keep reference to this window
        Wicket.Window.current = this;
    },

    close: function(force) {
        var that = this;
        $(this.window).on('hidden.bs.modal', function(e) {
            $(that.settings.element).remove();
            $(this).removeData('bs.modal');
            $(this).empty();

            Wicket.Window.prevWin = null;
            // clean references to elements
            that.window = that.classElement = that.caption = that.bottomLeft = that.bottomRight = that.bottom =
                that.left = that.right = that.topLeft = that.topRight = that.top = that.captionText = null;
        });
        $(this.window).modal('hide');

        if (this.oldWindow != null) {
            // set the old as current
            Wicket.Window.current = this.oldWindow;
            // increase it's z-index so that it's moved above the mask
//            Wicket.Window.current.window.style.zIndex = Wicket.Window.Mask.zIndex + 1;
            this.oldWindow = null;
        }
        else {
            // remove reference to the window
            Wicket.Window.current = null;
        }
    },

    destroy: function() {
        this.settings = null;
    }
}

/**
 * Counter for generating unique component ids.
 */
Wicket.Window.idCounter = 0;

/**
 * Returns the modal window markup with specified element identifiers.
 */
Wicket.Window.getMarkup = function(idWindow, idClassElement, idCaption, idContent, idTop, idTopLeft, idTopRight, idLeft, idRight, idBottomLeft, idBottomRight, idBottom, idCaptionText) {
    var s =
        '<div jid="mainWindow" data-backdrop="true" class="modal fade zoom" id=\"' + idWindow + '\">' +
        '<div class="modal-dialog ">' +
        '<div class="modal-content">' +
        '<div class="modal-header">' +
        '<button type="button" class="close" data-dismiss="modal" aria-label="Close">&times;</button>'+
        '<h4 class="modal-title" id=\"' + idCaptionText +  '\"></h4>' +
        '</div>' +
        '<div class="modal-body">' +
        '<div class="container-fluid" id=\"' + idContent + '\"></div>' +
        '</div>' +
        '<div class="modal-footer">' +
        '</div>' +
        '</div>' +
        '</div>' +
        '</div>';

    return s;
};