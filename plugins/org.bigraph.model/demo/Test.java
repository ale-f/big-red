import org.bigraph.model.Edge;
import org.bigraph.model.Node;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Bigraph;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Signature;

import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;

import org.bigraph.model.changes.descriptors.BoundDescriptor;

import org.bigraph.model.savers.BigraphXMLSaver;
import org.bigraph.model.savers.SaveFailedException;

import java.util.Arrays;

public class Test {
	public static void main(String[] args) {
		ChangeGroup cg = new ChangeGroup();

		Signature s = new Signature();
		Control c = new Control();
		PortSpec p = new PortSpec();

		Control.Identifier cI = new Control.Identifier("Zone");
		PortSpec.Identifier psI = new PortSpec.Identifier("z", cI);

		try {
			cg.addAll(Arrays.asList(
				s.changeAddControl(c, "Zone"),
				c.changeAddPort(p, "z"),
				new BoundDescriptor(s,
					new PortSpec.ChangeNameDescriptor(
						psI, "0"))));
			s.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			cre.printStackTrace();
			System.exit(-1);
		}

		cg.clear();

		Bigraph b = new Bigraph();
		Edge e = new Edge();
		Root r = new Root();
		Node n = new Node(c), o = new Node(c);
		b.setSignature(s);

		try {
			cg.addAll(Arrays.asList(
				b.changeAddChild(e, "e"),
				b.changeAddChild(r, "0"),
				r.changeAddChild(n, "n"),
				r.changeAddChild(o, "o")));

			Port
				n0 = n.getPort("0"),
				o0 = o.getPort("0");
			cg.addAll(Arrays.asList(
				n0.changeConnect(e),
				o0.changeConnect(e)));

			b.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			cre.printStackTrace();
			System.exit(-1);
		}
		
		BigraphXMLSaver bx = new BigraphXMLSaver();
		bx.setModel(b).setOutputStream(System.out);
		try {
			bx.exportObject();
		} catch (SaveFailedException sfe) {
			sfe.printStackTrace();
			System.exit(-1);
		}
	}
}
