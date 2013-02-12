package it.uniud.bigredit.command;

import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;


import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.model.LayoutUtilities;



public class LayoutableCreateCommand extends ChangeCommand {
	ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private ModelObject container = null;
	private ModelObject node = null;
//	
//	@Override
//	public boolean canExecute(){
//		System.out.println(super.canExecute());
//		return true;
//	}
//	
	
	
	@Override
	public void prepare() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return;
		
		if (container instanceof Bigraph){
			setContext(((Bigraph) container).getBigraph());
		}else if(container instanceof BRS){
			
		}else if(container instanceof Layoutable){
			setContext(((Layoutable)container).getBigraph());
		}else if (container instanceof Reaction){
			
		}
		
		if (container instanceof Container) {
			
			for (Layoutable i : ((Container) container).getChildren()) {
				if (i instanceof Edge)
					continue;
				else if (LayoutUtilities.getLayout(i).intersects(layout))
					return;
			}
		}
		if (container instanceof Bigraph)
			/* enforce boundaries */;
		
		if (container instanceof Bigraph) {
			if (node instanceof Root){
				String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
				cg.add(((Bigraph) container).changeAddChild(((Root)node), name));
				cg.add(new BoundDescriptor((Bigraph) container,
						new LayoutUtilities.ChangeLayoutDescriptor(
								((Root)node).getIdentifier().getRenamed(name),
								null, layout)));
			}else{
				String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
				cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), name));
				cg.add(new BoundDescriptor((Bigraph) container,
						new LayoutUtilities.ChangeLayoutDescriptor(
								((Layoutable)node).getIdentifier().getRenamed(name),
								null, layout)));
			}
			/** TODO add name */
			//String name = ((Bigraph) container).getBigraph().getFirstUnusedName((Layoutable)node);
			//cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), name), ((Layoutable)node).changeLayout(layout));
			//cg.add(((Bigraph) container).changeAddChild(((Layoutable)node), "R0"), ((Layoutable)node).changeLayout(layout));
		}
		if (container instanceof BRS){
			/** TODO get a name for Bigraph */
			
			cg.add(((BRS)container).changeAddChild(node, "B0"));
			cg.add(((BRS)container).changeLayoutChild(node, layout));
			
			
			
			
			if(node instanceof Reaction){
				cg.add( ((BRS)container).changeInsideModel(node, 
						((Reaction)node).changeLayoutChild(
						((Reaction)node).getRedex(), 
						new Rectangle(15, Reaction.MIN_HIGHT_BIG, layout.width/2-40, layout.height-100))));
				
				
				cg.add( ((BRS)container).changeInsideModel(node, 
						((Reaction)node).changeLayoutChild(
						((Reaction)node).getReactum(), 
						new Rectangle(layout.width/2+30, Reaction.MIN_HIGHT_BIG, (layout.width/2)-40, layout.height-100))));
			}
			
			if(node instanceof Bigraph){
				Root root= new Root();
				String name = ((Bigraph) node).getBigraph().getFirstUnusedName(root);
				
				cg.add(((Bigraph) node).changeAddChild(root, name));
				cg.add(new BoundDescriptor((Bigraph) container,
						new LayoutUtilities.ChangeLayoutDescriptor(
								((Layoutable)node).getIdentifier().getRenamed(name),
								null, new Rectangle(
										layout.x+10,layout.y+10,
										layout.width-20,layout.height-20))));
			}
			
		}
		
		
		if ((node instanceof OuterName)||(node instanceof InnerName)){
			if (layout.width < 20) {layout.width=20;}
			if (layout.height< 20) {layout.height=20;}
		}
		
		if (node instanceof Root){
			if (layout.width < 40) {layout.width=40;}
			if (layout.height< 40) {layout.height=40;}
		}
		
		
		if (container instanceof Reaction){
			/** TODO get a name for Bigraph */
			System.out.println("Instance of Reaction");
			if(layout.x > ((Reaction)container).SEPARATOR_WIDTH){
				cg.add(((Reaction) container).changeAddReactum((Bigraph) node));
				cg.add(((Reaction) container).changeLayoutChild(
												(Bigraph) node, layout));
			}else{
				cg.add(((Reaction) container).changeAddRedex((Bigraph) node));
				cg.add(((Reaction) container).changeLayoutChild(
												(Bigraph) node, layout));
			}
		}
	}
	
	public void setObject(Object s) {
		if (s instanceof Layoutable){
			node = (Layoutable)s;
		}
		
		if (s instanceof ModelObject){
			node = (ModelObject)s;
		}
	}
	
	public void setContainer(Object e) {

		if (e instanceof Container){
			container = (Container)e;
		}else if(e instanceof ModelObject){
			container = (ModelObject)e;
		}
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle)
			layout = (Rectangle)r;

	}

}
