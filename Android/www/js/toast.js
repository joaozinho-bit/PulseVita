class Toast {


    constructor() {


        this.container = document.createElement("div");

        this.container.className = "toast-container";


        document.body.appendChild(this.container);



        this.types = {


            success: {

                title:"Sucesso",

                icon:"bi-check-circle-fill"

            },


            error: {

                title:"Erro",

                icon:"bi-x-circle-fill"

            },


            warning: {

                title:"Aviso",

                icon:"bi-exclamation-triangle-fill"

            },


            info: {

                title:"Informação",

                icon:"bi-info-circle-fill"

            }


        };


    }





    show(message, type="info", duration=4000) {


        const data = this.types[type];

        if (
            "vibrate" in navigator &&
            (type === "error" || type === "warning")
        ) {
            navigator.vibrate(25);
        }


        const toast = document.createElement("div");


        toast.className = `toast ${type}`;



        toast.innerHTML = `

            <div class="toast-content">


                <i class="toast-icon bi ${data.icon}"></i>


                <div class="toast-text">

                    <div class="toast-title">

                        ${data.title}

                    </div>


                    <div class="toast-message">

                        ${message}

                    </div>


                </div>



                <i class="toast-close bi bi-x-lg"></i>


            </div>


            <div class="toast-progress"></div>

        `;



        this.container.appendChild(toast);



        const progress = toast.querySelector(".toast-progress");



        let remaining = duration;
        let startTime = null;
        let timer = null;
        let progressValue = 1;



        const remove = () => {


            if(toast.classList.contains("hide"))

                return;



            toast.classList.remove("show");

            toast.classList.add("hide");



            setTimeout(()=>{

                toast.remove();

            },400);


        };





        const startTimer = () => {

            startTime = Date.now();

            timer = setTimeout(remove, remaining);


            progress.style.transition = 
                `transform ${remaining}ms linear`;

            progress.style.transformOrigin = "left";

            progress.style.transform =
                `scaleX(${progressValue})`;


            requestAnimationFrame(() => {

                progress.style.transform = "scaleX(0)";

            });

        };





        const pauseTimer = () => {


            clearTimeout(timer);


            const elapsed = Date.now() - startTime;


            remaining -= elapsed;



            const computedStyle =
                window.getComputedStyle(progress);



            const matrix =
                new DOMMatrix(computedStyle.transform);



            progressValue = matrix.a;



            progress.style.transition = "none";


            progress.style.transform =
                `scaleX(${progressValue})`;


        };





        toast.addEventListener(
            "mouseenter",
            pauseTimer
        );



        toast.addEventListener(
            "mouseleave",
            startTimer
        );





        toast.querySelector(".toast-close")
        .onclick = () => {


            clearTimeout(timer);


            remove();


        };





        requestAnimationFrame(()=>{


            toast.classList.add("show");


            startTimer();


        });



    }






    success(message,duration=4000){

        this.show(message,"success",duration);

    }



    error(message,duration=4000){

        this.show(message,"error",duration);

    }



    warning(message,duration=4000){

        this.show(message,"warning",duration);

    }



    info(message,duration=4000){

        this.show(message,"info",duration);

    }


}



const toast = new Toast();