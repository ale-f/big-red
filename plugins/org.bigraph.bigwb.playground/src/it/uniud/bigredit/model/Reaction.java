package it.uniud.bigredit.model;


import org.eclipse.draw2d.geometry.Rectangle;

import it.uniud.bigredit.model.BRS.ChangeLayoutChild;
import it.uniud.bigredit.model.BRS.ChangeRemoveChild;
import it.uniud.bigredit.policy.BRSChangeValidator;
import it.uniud.bigredit.policy.ReactionChangeValidator;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ModelObject.ModelObjectChange;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeExecutor;
import dk.itu.big_red.model.changes.IChangeValidator;






public class Reaction  extends ModelObject  implements IChangeExecutor{
	
	public static final int GAP_WIDTH = 96;
	public static final int MIN_WIDTH_BIG= 100;
	public static final int MIN_HIGHT_BIG = 100;
	
	public static final String PROPERTY_RULE = "Reaction_Rule_Change";
	public static final String PROPERTY_RULE_LAYOUT = "Reaction_Rule_Change_Layout";
	
	private Bigraph redex = null;
	private Bigraph reactum = null;
	private Rectangle redexLayout = new Rectangle(0,0,100,300);
	private Rectangle reactumLayout = new Rectangle(100,0,100,300);;
	
	
	public Bigraph getRedex()
	{
		return redex;
	}
	
	public Bigraph getReactum()
	{
		return reactum;
	}
	
	
	public class ChangeAddRedex extends ModelObjectChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddRedex(Bigraph child) {
			System.out.println("new ChangeAddRedex");
			this.child = child;
		}
		
		@Override
		public ChangeAddRedex inverse() {
			return new ChangeAddRedex(oldchild);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);// && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add redex " + child + " to parent " + getCreator() + "\")";
		}
	}
	
	public class ChangeAddReactum extends ModelObjectChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddReactum(Bigraph child) {
			System.out.println("new ChangeAddReactum");
			this.child = child;
		}
		
		@Override
		public ChangeAddReactum inverse() {
			return new ChangeAddReactum(oldchild);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);// && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add reactum" + child + " to parent " + getCreator() + "\")";
		}
	}
	
	
	public class ChangeLayoutChild extends ModelObjectChange {
		public Bigraph child;
		public Rectangle layout;
		
		public ChangeLayoutChild(Bigraph child, Rectangle layout) {
			System.out.println(layout);
			this.child = child;
			this.layout=layout;
		}
		
		private Rectangle oldLayout = null;
		
		@Override
		public void beforeApply() {
			//oldName = child.getName();
		}
		
		@Override
		public boolean canInvert() {
			return (oldLayout != null);
		}
		
		@Override
		public ChangeLayoutChild inverse() {
			return new ChangeLayoutChild(child, oldLayout);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);
		}
		
		@Override
		public String toString() {
			return "Change(change layout of: " + child + " in " + getCreator() + ")";
		}
	}
	
	
	

	
	private void changeRedex(Bigraph redex ){
		Bigraph oldRedex = this.redex;
		this.redex= redex;
		firePropertyChange(Reaction.PROPERTY_RULE,oldRedex, redex);
	}
	
	private void changeReactum(Bigraph reactum){
		Bigraph oldReactum= this.reactum;
		
		this.reactum=reactum;
		firePropertyChange(Reaction.PROPERTY_RULE,oldReactum, reactum);
	}
	
	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		
		tryValidateChange(b);
		doChange(b);
		
	}

	private IChangeValidator validator = new ReactionChangeValidator(this);
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		
		validator.tryValidateChange(b);
	}
	
	private void doChange(Change b) {

		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup) b)
				doChange(c);
			System.out.println("here");
		} else if (b instanceof Reaction.ChangeAddReactum) {

			Reaction.ChangeAddReactum c = (Reaction.ChangeAddReactum) b;
			((Reaction) c.getCreator()).changeReactum(c.child);
			
		} else if (b instanceof Reaction.ChangeAddRedex) {
			Reaction.ChangeAddRedex c = (Reaction.ChangeAddRedex) b;
			((Reaction) c.getCreator()).changeRedex(c.child);
		}else if(b instanceof Reaction.ChangeLayoutChild){
			Reaction.ChangeLayoutChild c = (Reaction.ChangeLayoutChild)b;
			((Reaction)c.getCreator())._changeLayoutChild(c.child, c.layout);
		}
	}
	
	
	public Change changeAddRedex(Bigraph node) {
		return new ChangeAddRedex(node);
	}
	
	public Change changeAddReactum(Bigraph node) {
		return new ChangeAddReactum(node);
	}
	
	public Change changeLayoutChild(Bigraph node, Rectangle rectangle) {
		return new ChangeLayoutChild(node,rectangle);
	}
	
	public void _changeLayoutChild(ModelObject child, Rectangle rectangle) {
		
		System.out.println("change child layout Reaction -> Redex || Reactum");
		//addChild(child);
		Rectangle oldRect= null;
		if (child.equals(redex)){
			oldRect=redexLayout;
			redexLayout=rectangle;
		}else if (child.equals(reactum)){
			oldRect=reactumLayout;
			reactumLayout=rectangle;
		}
		firePropertyChange(Reaction.PROPERTY_RULE_LAYOUT,oldRect, rectangle);//children.get(child), rectangle);
	}

	

	public Rectangle getChildConstraint(Bigraph child){
		if (child.equals(redex)){
			return redexLayout;
		}else if (child.equals(reactum)){
			return reactumLayout;
		}
		return new Rectangle(100,100,100,100);
		
	}
	
	/*@Override
	public boolean addChild( BaseNode child )
	{
		if ( child instanceof Graph ) {
			if ( redex == null )
				redex = ( Graph )child;
			else if ( reactum == null )
				reactum = ( Graph )child;
		}
		return super.addChild( child );
	}
	
	public boolean addChild( BaseNode child, boolean isReactum )
	{
		if ( child instanceof Graph ) {
			if ( reactum == null )
				reactum = ( Graph )child;
			else if ( redex == null )
				redex = ( Graph )child;
		}
		return super.addChild( child );
	}
	
	@Override
	public boolean removeChild( BaseNode child )
	{
		if ( child instanceof Graph ) {
			if ( child == redex )
				redex = null;
			if ( child == reactum )
				reactum = null;
		}
		return super.removeChild( child );
	}
	
	@Override
	public void organiseLayout( HashMap< BaseNode, Rectangle > old )
	{
		for ( BaseNode t : getChildren() )
			t.organiseLayout( old );
		if ( !old.containsKey( this ) )
			old.put( this, new Rectangle( getLayout() ) );
		Rectangle r1;
		Rectangle r2;
		if ( redex != null ) {
			if ( !old.containsKey( redex ) )
				old.put( redex, redex.getLayout() );
			r1 = new Rectangle( redex.getLayout() );
		}
		else
			r1 = new Rectangle( 0, 0, GAP_WIDTH, GAP_WIDTH );
		if ( reactum != null ) {
			if ( !old.containsKey( reactum ) )
				old.put( reactum, reactum.getLayout() );
			r2 = new Rectangle( reactum.getLayout() );
		}
		else
			r2 = new Rectangle( 0, 0, GAP_WIDTH, GAP_WIDTH );
		
		int width = r1.width + r2.width + GAP_WIDTH + 4 * MARGIN;
		int height = Math.max( r1.height, r2.height ) + 4 * MARGIN;
		
		Rectangle r = new Rectangle( getLayout() );
		if ( r.width != width ) {
			r.x += ( r.width - width ) / 2;
			r.width = width;
		}
		if ( r.height != height ) {
			r.y += ( r.height - height ) / 2;
			r.height = height;
		}
		
		r1.x = 2 * MARGIN;
		r1.y = 2 * MARGIN + ( Math.max( r1.height, r2.height ) - r1.height ) / 2;
		r2.x = 2 * MARGIN + r1.width + GAP_WIDTH;
		r2.y = 2 * MARGIN + ( Math.max( r1.height, r2.height ) - r2.height ) / 2;
		setLayout( r );
		if ( redex != null )
			redex.setLayout( r1 );
		if ( reactum != null )
			reactum.setLayout( r2 );
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		Reaction reaction = new Reaction();
		reaction.setName( getName() );
		reaction.setDescription( getDescription() );
		reaction.setLayout( new Rectangle( getLayout() ) );

		for ( BaseNode base : getChildren() ) {
			BaseNode clone = ( BaseNode )base.clone();
			if ( redex == null && reactum != null )
				reaction.addChild( clone, true );
			else
				reaction.addChild( clone );
			clone.setLayout( base.getLayout() );
		}
		
		return reaction;
	}
	
	@Override
	public void copyName()
	{
		setName( getCopyName( Reaction.class ) );
	}
	
	@Override
	public void save( Writer out ) throws IOException
	{
		out.start( "reaction" );
		out.write( "prop-name", getName() );
		out.write( "prop-desc", getDescription() );
		out.write( "prop-layout", getLayout() );
		out.write( "prop-reactum", redex == null && reactum != null );
		super.save( out );
		out.end();
	}*/

}