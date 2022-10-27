package ift4055.factories;

import ift4055.elements.Element;

public class ElementFactory {
        private static ElementFactory single_instance = null;
        private ElementFactory(){}
        public static ElementFactory GetFactory(){
            if(single_instance==null) single_instance = new ElementFactory();
            return single_instance;
        }

        //use getShape method to get object of type shape
        /*
        public Element getShape(String shapeType){
            if(shapeType == null){
                return null;
            }
            if(shapeType.equalsIgnoreCase("CIRCLE")){
                return new Circle();

            } else if(shapeType.equalsIgnoreCase("RECTANGLE")){
                return new Rectangle();

            } else if(shapeType.equalsIgnoreCase("SQUARE")){
                return new Square();
            }

            return null;
        }*/
}
